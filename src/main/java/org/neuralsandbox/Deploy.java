package org.neuralsandbox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.plugin.ArtifactFilter;
import org.mule.tools.maven.plugin.Exclusion;
import org.mule.tools.maven.plugin.Inclusion;
import org.mule.tools.maven.plugin.MuleArchiver;

/**
 * @goal deploy
 * @execute phase="compile"
 * @requiresDirectInvocation true
 * @requiresDependencyResolution runtime 
 */
public class Deploy extends AbstractMojo {
	public static final String DEFAULT_NAME = "MuleApplication";

    /**
     * The Maven project. Needed for information about dependencies.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
    
	/**
	 * The name that the application will be deployed as.
	 * Default is "MuleApplication"
	 * 
	 * @parameter expression="${name}" 
	 */
	private String name;
	
	/**
	 * The version that the application will be deployed as.
	 * Default is the current time in milliseconds.
	 * 
	 * @parameter expression="${version}"
	 */
	private String version;
	
	/**
	 * The username that has 
	 * 
	 * @parameter expression="${username}"
	 * @required
	 */	
	private String username;
	
	/**
	 * @parameter expression="${password}"
	 * @required
	 */
	private String password;
	
    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;	

    /**
     * Directory containing the app resources.
     *
     * @parameter expression="${basedir}/src/main/app"
     * @required
     */
    protected File appDirectory;
    
	/**
	 * @parameter
	 */
	private URL muleRepositoryUrl;
    
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(name == null) {
			getLog().info("Name is not set, using default \"" + DEFAULT_NAME + "\"");
			name = DEFAULT_NAME;
		}
		if(version == null) {		
			version = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			getLog().info("Version is not set, using a default of the timestamp: " + version);
		}
		if(username == null || password == null) {
			throw new MojoFailureException((username == null ? "Username" : "Password") + " not set.");
		}
		try {
			validateProject();
			File file = File.createTempFile("mule", ".zip");
			MuleArchiver archiver = new MuleArchiver();
			
			archiver.setDestFile(file);
			
			archiver.addResources(appDirectory);
			try {
				archiver.addClasses(classesDirectory, null, null);
			} catch (Exception e) {
				getLog().info("No classes are present, but still compiling apps.");
			}
			ArtifactFilter filter = new ArtifactFilter(project, (List<Inclusion>)new ArrayList<Inclusion>(), (List<Exclusion>)new ArrayList<Exclusion>(), true);
			for(Artifact artifact : filter.getArtifactsToArchive()){
				archiver.addLib(artifact.getFile());
				getLog().info("Added: " + artifact.getArtifactId());
			}
			archiver.createArchive();
			
			RESTSend(file);
		} catch (Exception e) {
			throw new MojoFailureException("Error in attempting to deploy archive: " + e.toString(), e);
		}
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
    
	private void RESTSend(File packageFile) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(muleRepositoryUrl.toString());
		String authentication_encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
		
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("file", new FileBody(packageFile));
		entity.addPart("name", new StringBody(name));
		entity.addPart("version", new StringBody(version));
		
		post.addHeader("Authorization", "Basic " + authentication_encoded);
		post.setEntity(entity);
					
		HttpResponse response = client.execute(post);
		
		int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode == 200 || statusCode == 201) {
			getLog().info("The operation was successful.");
			getLog().info("Response: " + EntityUtils.toString(response.getEntity()));
		} else if (statusCode == 404) {
			HttpResponseException he = new HttpResponseException(404, "The resource was not found.");
			getLog().error("Status Line: " + response.getStatusLine());
			throw he;
		} else if (statusCode == 409) {
			HttpResponseException he = new HttpResponseException(409, "The operation was unsuccessful because a resource with that name already exists.");
			getLog().error("Status Line: " + response.getStatusLine());
			throw he;
		} else if (statusCode == 500){
			HttpResponseException he = new HttpResponseException(500, "The operation was unsuccessful.");
			getLog().error("Full HTTP Body: " + EntityUtils.toString(response.getEntity()));
			throw he;
		} else {
			HttpResponseException he = new HttpResponseException(statusCode, "Unexpected Status Code Return, Status Line: " + response.getStatusLine());
			getLog().error("Full HTTP Body: " + EntityUtils.toString(response.getEntity()));
			throw he;
		}
	}
}
