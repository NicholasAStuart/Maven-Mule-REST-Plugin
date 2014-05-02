package org.mule.tools.maven.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.cxf.helpers.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class MuleRestTest {

    private static final String APPLICATION_VERSION_ID = "local$66b3cf20-6e76-4fd9-8dc6-a50a804069a0";

	@Rule
    public WireMockRule wireMockRule = new WireMockRule(12312);

    public static MuleRest muleRest;

    @BeforeClass
    public static void init() throws MalformedURLException {
	muleRest = new MuleRest(new URL("http://0.0.0.0:12312"), "admin", "admin");
    }

    private String generateDeploymentIdJson(String name, String id) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeNumberField("total", 1L);
	jsonGenerator.writeFieldName("data");

	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", name);
	jsonGenerator.writeStringField("id", id);
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();

	jsonGenerator.writeEndObject();
	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }

    private String generateDeploymentRequestJson(String serverId, String name, String versionId) throws JsonGenerationException, IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jfactory = new JsonFactory();
	JsonGenerator jsonGenerator = jfactory.createJsonGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", name);

	jsonGenerator.writeFieldName("servers");
	jsonGenerator.writeStartArray();
	jsonGenerator.writeString(serverId);
	jsonGenerator.writeEndArray();

	jsonGenerator.writeFieldName("applications");
	jsonGenerator.writeStartArray();
	jsonGenerator.writeString(versionId);
	jsonGenerator.writeEndArray();

	jsonGenerator.writeEndObject();
	jsonGenerator.close();

	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }

    private String generateDeploymentResponseJson(String deploymentId) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("id", deploymentId);

	jsonGenerator.writeEndObject();

	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }

    private String generateServerGroupIdJson(String name, String id) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeNumberField("total", 1L);
	jsonGenerator.writeFieldName("data");

	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", name);
	jsonGenerator.writeStringField("id", id);
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();

	jsonGenerator.writeEndObject();
	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }
    
    private String generateApplicationsJson(String applicationName, String version) throws Exception{
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
	
	jsonGenerator.writeStartObject();
	jsonGenerator.writeNumberField("total", 1L);
	jsonGenerator.writeFieldName("data");
	
	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", applicationName);
	jsonGenerator.writeStringField("id", "local$0edb159a-5961-4384-bdf8-6ebfc5b9d6bf");
	jsonGenerator.writeStringField("href", "http://localhost:8080/mmc/api/repository/local$0edb159a-5961-4384-bdf8-6ebfc5b9d6bf");
	
	jsonGenerator.writeFieldName("versions");
	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", version);
	jsonGenerator.writeStringField("id", APPLICATION_VERSION_ID);
	jsonGenerator.writeStringField("parentPath", "/Applications/mule-example-hello");
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();
	
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();
	
	jsonGenerator.writeEndObject();
	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();
	
	return json;
    }

    private String generateServersJson(String serverGroupToFind, String serverId) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeNumberField("total", 1L);
	jsonGenerator.writeFieldName("data");

	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("id", serverId);
	jsonGenerator.writeFieldName("groups");
	jsonGenerator.writeStartArray();
	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("name", serverGroupToFind);
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();
	jsonGenerator.writeEndObject();
	jsonGenerator.writeEndArray();

	jsonGenerator.writeEndObject();
	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }

    private String generateUploadedPackageJson(String versionId, String applicationId) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonFactory jsonFactory = new JsonFactory();
	JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);

	jsonGenerator.writeStartObject();
	jsonGenerator.writeStringField("versionId", versionId);
	jsonGenerator.writeStringField("applicationId", applicationId);

	jsonGenerator.writeEndObject();

	jsonGenerator.close();
	String json = stringWriter.toString();
	stringWriter.close();

	return json;
    }

    private void stubCreateDeployment(String deploymentId) throws IOException {
	stubFor(post(urlEqualTo("/deployments")).willReturn(aResponse().withStatus(200)
		.withHeader("Content-Type", "application/json")
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
		.withBody(generateDeploymentResponseJson(deploymentId))));
    }

    private void stubDeleteDeploymentById(String deploymentId) {
	stubFor(delete(urlEqualTo("/deployments/" + deploymentId)).willReturn(aResponse().withStatus(200)
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")));
    }

    private void stubGetServers(String serverGroupToFind, String serverId) throws IOException {
	stubFor(get(urlEqualTo("/servers")).willReturn(aResponse().withStatus(200)
		.withHeader("Content-Type", "application/json")
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
		.withBody(generateServersJson(serverGroupToFind, serverId))));
    }

    private void stubGetDeploymentIdByName(String name, String id) throws IOException {
	stubFor(get(urlEqualTo("/deployments")).willReturn(aResponse().withStatus(200)
		.withHeader("Content-Type", "application/json")
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
		.withBody(generateDeploymentIdJson(name, id))));
    }

    @Test
    public void testRestfullyCreateDeployment() throws IOException {
	String serverGroup = UUID.randomUUID()
		.toString();
	String name = UUID.randomUUID()
		.toString();
	String versionId = UUID.randomUUID()
		.toString();
	String serverId = UUID.randomUUID()
		.toString();
	String deploymentId = UUID.randomUUID()
		.toString();

	stubGetServers(serverGroup, serverId);
	stubCreateDeployment(deploymentId);
	stubGetDeploymentIdByName(name, deploymentId);
	stubDeleteDeploymentById(deploymentId);
	muleRest.restfullyCreateDeployment(serverGroup, name, versionId);
	verifyDeleteDeploymentById(deploymentId);
	verifyGetDeploymentIdByName();
	verifyGetServers();
	verifyCreateDeployment(serverId, name, versionId);
    }

    @Test
    public void testRestfullyDeleteDeployment() throws IOException {
	String name = UUID.randomUUID()
		.toString();
	String deploymentId = UUID.randomUUID()
		.toString();

	stubGetDeploymentIdByName(name, deploymentId);
	stubDeleteDeploymentById(deploymentId);
	muleRest.restfullyDeleteDeployment(name);
	verifyGetDeploymentIdByName();
	verifyDeleteDeploymentById(deploymentId);
    }

    @Test
    public void testRestfullyDeleteDeploymentById() throws IOException {
	String deploymentId = UUID.randomUUID()
		.toString();
	stubDeleteDeploymentById(deploymentId);
	muleRest.restfullyDeleteDeploymentById(deploymentId);

    }

    @Test
    public void testRestfullyDeployDeploymentById() throws IOException {
	String deploymentId = UUID.randomUUID()
		.toString();

	stubFor(post(urlEqualTo("/deployments/" + deploymentId + "/deploy")).willReturn(aResponse().withStatus(200)
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")));

	muleRest.restfullyDeployDeploymentById(deploymentId);

	verify(postRequestedFor(urlEqualTo("/deployments/" + deploymentId + "/deploy")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
    }

    @Test
    public void testRestfullyGetDeploymentIdByName() throws IOException {
	String name = UUID.randomUUID()
		.toString();
	String id = UUID.randomUUID()
		.toString();

	stubGetDeploymentIdByName(name, id);
	String depoymentId = muleRest.restfullyGetDeploymentIdByName(name);
	Assert.assertEquals("Deployment Id doesn't match", depoymentId, id);
	verifyGetDeploymentIdByName();
    }

    @Test
    public void testRestfullyGetServerGroupId() throws IOException {
	String name = UUID.randomUUID()
		.toString();
	String id = UUID.randomUUID()
		.toString();

	stubFor(get(urlEqualTo("/serverGroups")).willReturn(aResponse().withStatus(200)
		.withHeader("Content-Type", "application/json")
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
		.withBody(generateServerGroupIdJson(name, id))));

	String groupId = muleRest.restfullyGetServerGroupId(name);
	Assert.assertEquals("Group Id doesn't match", groupId, id);

	verify(getRequestedFor(urlMatching("/serverGroups")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
    }

    @Test
    public void testRestfullyGetServers() throws IOException {
	String serverGroupToFind = UUID.randomUUID()
		.toString();
	String serverId = UUID.randomUUID()
		.toString();

	stubGetServers(serverGroupToFind, serverId);
	Set<String> servers = muleRest.restfullyGetServers(serverGroupToFind);
	Assert.assertTrue("Server Id doesn't match", servers.contains(serverId));
	verifyGetServers();
    }
    
    @Test
    public void testRestfullyGetApplicationId() throws Exception{
    String applicationName = "My_Mule_App";
    String version = "1.0-SNAPSHOT";
    
    stubFor(get(urlEqualTo("/repository")).willReturn(aResponse().withStatus(200)
    		.withHeader("Content-Type", "application/json")
    		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
    		.withBody(generateApplicationsJson(applicationName, version))));
    
    String versionId = muleRest.restfullyGetApplicationId(applicationName, version);
    
    assertEquals(APPLICATION_VERSION_ID, versionId);
	verify(getRequestedFor(urlMatching("/repository")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
	
	assertNull(muleRest.restfullyGetApplicationId(applicationName, "wrong version"));
	assertNull(muleRest.restfullyGetApplicationId("wrong application name", version));	
    }
    
    @Test
    public void testRestfullyDeleteApplicationById() throws Exception{
    stubFor(delete(urlEqualTo("/repository/"+APPLICATION_VERSION_ID)).willReturn(aResponse().withStatus(200)
    		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")));
    muleRest.restfullyDeleteApplicationById(APPLICATION_VERSION_ID);
    verify(deleteRequestedFor(urlEqualTo("/repository/"+APPLICATION_VERSION_ID)));
    }

    @Test
    public void testRestfullyUploadRepository() throws Exception {
	String versionId = UUID.randomUUID()
		.toString();
	String applicationId = UUID.randomUUID()
		.toString();
	String name = UUID.randomUUID()
		.toString();
	String version = UUID.randomUUID()
		.toString();

	File file = File.createTempFile("prefix", "suffix");
	InputStream inputStream = new FileInputStream(file);
	String fileContent = IOUtils.toString(inputStream);

	stubFor(post(urlEqualTo("/repository")).willReturn(aResponse().withStatus(200)
		.withHeader("Content-Type", "application/json")
		.withHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
		.withBody(generateUploadedPackageJson(versionId, applicationId))));

	String returnedVersion = muleRest.restfullyUploadRepository(name, version, file);
	Assert.assertEquals("Version Id doesn't match", versionId, returnedVersion);

	verify(postRequestedFor(urlMatching("/repository")).withHeader("Content-Type", containing("multipart/form-data"))
		.withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4="))
		.withRequestBody(containing("Content-Type: text/plain\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <name>\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n" + name + "\r\n"))
		.withRequestBody(containing("Content-Type: text/plain\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <version>\r\nContent-Disposition: form-data; name=\"version\"\r\n\r\n" + version + "\r\n"))
		.withRequestBody(containing("Content-Type: application/octet-stream\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <file>\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" + fileContent + "\r\n")));
    }
    
    @Test
    public void testRestfullyDeleteApplication() throws Exception{
	String applicationName = "My_Mule_App";
	String version = "1.0-SNAPSHOT";
	
	MuleRest muleRestSpy = spy(muleRest);
	doReturn(APPLICATION_VERSION_ID).when(muleRestSpy).restfullyGetApplicationId(applicationName, version);
	doNothing().when(muleRestSpy).restfullyDeleteApplicationById(anyString());
	
	muleRestSpy.restfullyDeleteApplication(applicationName, version);
	
	org.mockito.Mockito.verify(muleRestSpy).restfullyDeleteApplicationById(APPLICATION_VERSION_ID);
    }
    
    @Test
    public void testRestfullyDeleteApplicationDoesNotExist() throws Exception{
	String applicationName = "My_Mule_App";
	String futureVersion = "5.0-SNAPSHOT";
	
	MuleRest muleRestSpy = spy(muleRest);
	doReturn(null).when(muleRestSpy).restfullyGetApplicationId(applicationName, futureVersion);
	doNothing().when(muleRestSpy).restfullyDeleteApplicationById(anyString());
	
	muleRestSpy.restfullyDeleteApplication(applicationName, futureVersion);
	
	org.mockito.Mockito.verify(muleRestSpy, never()).restfullyDeleteApplicationById(anyString());
    }
    
    @Test
    public void testIsSnapshotVersion(){
    assertTrue(muleRest.isSnapshotVersion("1.0-SNAPSHOT"));
    assertTrue(muleRest.isSnapshotVersion("2.0.1-SNAPSHOT"));
    assertFalse(muleRest.isSnapshotVersion("1.0"));
    }

    private void verifyCreateDeployment(String serverId, String name, String versionId) throws IOException {
	verify(postRequestedFor(urlEqualTo("/deployments")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4="))
		.withRequestBody(equalTo(generateDeploymentRequestJson(serverId, name, versionId))));
    }

    private void verifyDeleteDeploymentById(String deploymentId) {
	verify(deleteRequestedFor(urlEqualTo("/deployments/" + deploymentId)).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
    }

    private void verifyGetDeploymentIdByName() {
	verify(getRequestedFor(urlMatching("/deployments")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
    }

    private void verifyGetServers() {
	verify(getRequestedFor(urlMatching("/servers")).withHeader("Authorization", equalTo("Basic YWRtaW46YWRtaW4=")));
    }
}