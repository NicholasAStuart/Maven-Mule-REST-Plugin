package org.neuralsandbox;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @goal deploy
 * @execute phase="compile"
 * @requiresDirectInvocation true
 * @requiresDependencyResolution runtime
 */
public class Deploy extends AbstractMojo {
	public static final String DEFAULT_NAME = "MuleApplication";

	/**
	 * Directory containing the generated Mule App.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	protected File outputDirectory;
	/**
	 * Name of the generated Mule App.
	 * 
	 * @parameter alias="appName" expression="${appName}"
	 *            default-value="${project.build.finalName}"
	 * @required
	 */
	protected String finalName;
	/**
	 * The Maven project. Needed for information about dependencies.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The name that the application will be deployed as. Default is
	 * "MuleApplication"
	 * 
	 * @parameter expression="${name}"
	 */
	protected String name;

	/**
	 * The version that the application will be deployed as. Default is the
	 * current time in milliseconds.
	 * 
	 * @parameter expression="${version}"
	 */
	protected String version;

	/**
	 * The username that has
	 * 
	 * @parameter expression="${username}"
	 * @required
	 */
	protected String username;

	/**
	 * @parameter expression="${password}"
	 * @required
	 */
	protected String password;

	/**
	 * Directory containing the app resources.
	 * 
	 * @parameter expression="${basedir}/src/main/app"
	 * @required
	 */
	protected File appDirectory;

	/**
	 * @parameter expression="muleApiUrl"
	 * @required
	 */
	protected URL muleApiUrl;

	/**
	 * @parameter expression="${serverGroup}"
	 * @required
	 */
	protected String serverGroup;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (name == null) {
			getLog().info("Name is not set, using default \"" + DEFAULT_NAME + "\"");
			name = DEFAULT_NAME;
		}
		if (version == null) {
			version = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			getLog().info("Version is not set, using a default of the timestamp: " + version);
		}
		if (username == null || password == null) {
			throw new MojoFailureException((username == null ? "Username" : "Password") + " not set.");
		}
		if (username == null || password == null) {
			throw new MojoFailureException((username == null ? "Username" : "Password") + " not set.");
		}
		if (outputDirectory == null) {
			throw new MojoFailureException("outputDirectory not set.");
		}
		if (finalName == null) {
			throw new MojoFailureException("finalName not set.");
		}
		if (serverGroup == null) {
			throw new MojoFailureException("serverGroup not set.");
		}
		try {
			validateProject();
			String versionId = restfullyUploadPackage(getMuleZipFile());
			String deploymentId = restfullyCreateDeployment(versionId);
			restfullyDeployDeploymentById(deploymentId);
			;
		} catch (Exception e) {
			throw new MojoFailureException("Error in attempting to deploy archive: " + e.toString(), e);
		}
	}

	protected File getMuleZipFile() throws MojoFailureException {
		getLog().info("outputDirectory = " + outputDirectory);
		getLog().info("finalName = " + finalName);
		File file = new File(this.outputDirectory, this.finalName + ".zip");
		if (!file.exists()) {
			throw new MojoFailureException("There no application ZIP file generated : check that you have configured the maven-mule-plugin to generated the this file");
		}
		return file;
	}

	private void validateProject() throws MojoExecutionException {
		File muleConfig = new File(appDirectory, "mule-config.xml");
		File deploymentDescriptor = new File(appDirectory, "mule-deploy.properties");

		if ((muleConfig.exists() == false) && (deploymentDescriptor.exists() == false)) {
			String message = String.format("No mule-config.xml or mule-deploy.properties in %1s", project.getBasedir());

			getLog().error(message);
			throw new MojoExecutionException(message);
		}
	}

	/**
	 * @return the id of the server group having the given serverGroup
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	protected String restfullyGetServerGroupId() throws ClientProtocolException, IOException {
		String serverGroupId = null;
		// create a http get request to {muleApiUrl}/serverGroups
		HttpGet get = new HttpGet(muleApiUrl.toString() + "/serverGroups");
		CloseableHttpClient httpClient = buildHttpClient(get);
		try {
			HttpResponse response = httpClient.execute(get);

			ObjectMapper mapper = new ObjectMapper();
			String string = EntityUtils.toString(response.getEntity());
			JsonNode jsonNode = mapper.readValue(string, JsonNode.class);
			JsonNode groupsNode = jsonNode.path("data");
			for (Iterator<JsonNode> iterator = groupsNode.iterator(); iterator.hasNext() && serverGroupId == null;) {
				JsonNode groupNode = iterator.next();
				if (serverGroup.equals(groupNode.path("name").asText())) {
					serverGroupId = groupNode.path("id").asText();
				}
			}
			if (serverGroupId == null) {
				throw new IllegalArgumentException("no server group found having the name " + serverGroup);
			}
			getLog().info("Deployment will be done to [" + serverGroup + "] : [" + serverGroupId + "]");
		} finally {
			httpClient.close();
		}
		return serverGroupId;
	}

	protected Set<String> restfullyGetServers() throws ClientProtocolException, IOException {
		Set<String> serversId = new HashSet<String>();
		HttpGet get = new HttpGet(muleApiUrl.toString() + "/servers");
		CloseableHttpClient httpClient = buildHttpClient(get);
		try {
			HttpResponse response = httpClient.execute(get);

			ObjectMapper mapper = new ObjectMapper();
			String string = EntityUtils.toString(response.getEntity());
			JsonNode jsonNode = mapper.readValue(string, JsonNode.class);
			JsonNode serversNode = jsonNode.path("data");
			for (Iterator<JsonNode> serverIterator = serversNode.iterator(); serverIterator.hasNext();) {
				JsonNode serverNode = serverIterator.next();
				String serverId = serverNode.path("id").asText();

				JsonNode groupsNode = serverNode.path("groups");
				for (Iterator<JsonNode> groupsIterator = groupsNode.iterator(); groupsIterator.hasNext();) {
					JsonNode groupNode = groupsIterator.next();
					if (serverGroup.equals(groupNode.path("name").asText())) {
						serversId.add(serverId);
					}
				}
			}
		} finally {
			httpClient.close();
		}
		return serversId;
	}

	protected String restfullyGetDeploymentId() throws ClientProtocolException, IOException {

		HttpGet get = new HttpGet(muleApiUrl.toString() + "/deployments");
		CloseableHttpClient httpClient = buildHttpClient(get);

		String deploymentId = null;
		try {
			HttpResponse response = httpClient.execute(get);

			ObjectMapper mapper = new ObjectMapper();
			String string = EntityUtils.toString(response.getEntity());
			JsonNode jsonNode = mapper.readValue(string, JsonNode.class);
			JsonNode deploymentsNode = jsonNode.path("data");
			for (Iterator<JsonNode> deploymentIterator = deploymentsNode.iterator(); deploymentIterator.hasNext();) {
				JsonNode deploymentNode = deploymentIterator.next();
				if (name.equals(deploymentNode.path("name").asText())) {
					deploymentId = deploymentNode.path("id").asText();
				}
			}
		} finally {
			httpClient.close();
		}
		return deploymentId;
	}

	protected void restfullyDeleteDeployment() throws ClientProtocolException, IOException {
		String deploymentId = restfullyGetDeploymentId();
		if (deploymentId == null) {
			getLog().info("No deployment found having the name [" + name + "] it will be created");
		} else {
			HttpDelete delete = new HttpDelete(muleApiUrl.toString() + "/deployments/" + deploymentId);
			CloseableHttpClient httpClient = buildHttpClient(delete);

			try {
				CloseableHttpResponse response = httpClient.execute(delete);
				processResponse(response);
				getLog().info("[" + name + "] deployment deleted successful.");
			} finally {
				httpClient.close();
			}
		}
	}

	protected void restfullyDeleteDeploymentById(String deploymentId) throws ClientProtocolException, IOException {

		HttpDelete delete = new HttpDelete(muleApiUrl.toString() + "/deployments/" + deploymentId);
		CloseableHttpClient httpClient = buildHttpClient(delete);

		try {
			CloseableHttpResponse response = httpClient.execute(delete);
			processResponse(response);
		} finally {
			httpClient.close();
		}
	}

	protected void restfullyDeployDeployment() throws ClientProtocolException, IOException {
		String deploymentId = restfullyGetDeploymentId();
		restfullyDeployDeploymentById(deploymentId);
	}

	protected void restfullyDeployDeploymentById(String deploymentId) throws ClientProtocolException, IOException {

		HttpPost post = new HttpPost(muleApiUrl.toString() + "/deployments/" + deploymentId + "/deploy");
		CloseableHttpClient httpClient = buildHttpClient(post);

		try {
			CloseableHttpResponse response = httpClient.execute(post);
			String responseObject = processResponse(response);
			getLog().info("[" + name + "] " + responseObject);
		} finally {
			httpClient.close();
		}
	}

	protected String restfullyCreateDeployment(String versionId) throws ClientProtocolException, IOException {
		getLog().info("Trying to deploy application versionId : " + versionId);
		Set<String> serversIds = restfullyGetServers();
		if (serversIds.isEmpty()) {
			throw new IllegalArgumentException("No server found into group : " + serverGroup);
		}
		// delete existing deployment before creating new one
		restfullyDeleteDeployment();

		HttpPost post = new HttpPost(muleApiUrl.toString() + "/deployments");
		CloseableHttpClient httpClient = buildHttpClient(post);
		try {

			StringWriter requestContent = new StringWriter();
			JsonFactory jfactory = new JsonFactory();
			JsonGenerator jGenerator = jfactory.createJsonGenerator(requestContent);
			jGenerator.writeStartObject(); // {
			jGenerator.writeStringField("name", name); // "name" : name
			jGenerator.writeFieldName("servers"); // "servers" :
			jGenerator.writeStartArray(); // [
			for (String serverId : serversIds) {
				jGenerator.writeString(serverId); // "serverId" 
			}
			jGenerator.writeEndArray(); // ]
			jGenerator.writeFieldName("applications"); // "applications" :
			jGenerator.writeStartArray(); // [
			jGenerator.writeString(versionId); // "applicationId" 
			jGenerator.writeEndArray(); // ]
			jGenerator.writeEndObject(); // }
			jGenerator.close();

			StringEntity entity = new StringEntity(requestContent.toString(), Charset.forName("UTF-8"));
			entity.setContentType("application/json");
			post.setEntity(entity);

			HttpResponse response = httpClient.execute(post);
			String responseObject = processResponse(response);
			getLog().info("[" + name + "] deployment created");
			getLog().debug(responseObject);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readValue(responseObject, JsonNode.class);
			return jsonNode.path("id").asText();
		} finally {
			httpClient.close();
		}
	}

	protected String restfullyUploadPackage(File packageFile) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(muleApiUrl.toString() + "/repository");
		CloseableHttpClient client = buildHttpClient(post);
		try {

			HttpEntity entity = MultipartEntityBuilder.create()
					.addTextBody("name", name)
					.addTextBody("version", version)
					.addBinaryBody("file", packageFile, ContentType.APPLICATION_OCTET_STREAM, version+packageFile.getName())
					.build();

			post.setEntity(entity);

			HttpResponse response = client.execute(post);

			String responseObject = processResponse(response);
			getLog().info("[" + name + "] application was uploaded successful.");
			getLog().debug("Response: " + responseObject);
			Map<String, String> result = new HashMap<String, String>();
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(responseObject, new TypeReference<Map<String, String>>() {
			});
			return result.get("versionId");
		} finally {
			client.close();
		}
	}

	private String processResponse(HttpResponse response) throws IOException, HttpResponseException {
		int statusCode = response.getStatusLine().getStatusCode();
		String responseObject = EntityUtils.toString(response.getEntity());
		if (statusCode == 200 || statusCode == 201) {
			return responseObject;
		} else if (statusCode == 404) {
			HttpResponseException he = new HttpResponseException(404, "The resource was not found.");
			getLog().error("Status Line: " + response.getStatusLine());
			throw he;
		} else if (statusCode == 409) {
			HttpResponseException he = new HttpResponseException(409, "The operation was unsuccessful because a resource with that name already exists.");
			getLog().error("Status Line: " + response.getStatusLine());
			throw he;
		} else if (statusCode == 500) {
			HttpResponseException he = new HttpResponseException(500, "The operation was unsuccessful.");
			getLog().error("Full HTTP Body: " + responseObject);
			throw he;
		} else {
			HttpResponseException he = new HttpResponseException(statusCode, "Unexpected Status Code Return, Status Line: " + response.getStatusLine());
			getLog().error("Full HTTP Body: " + responseObject);
			throw he;
		}
	}

	private CloseableHttpClient buildHttpClient(HttpRequest httpRequest) {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = clientBuilder.build();
		String authentication_encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
		httpRequest.addHeader("Authorization", "Basic " + authentication_encoded);
		return httpClient;
	}

}
