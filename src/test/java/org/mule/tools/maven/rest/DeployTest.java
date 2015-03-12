package org.mule.tools.maven.rest;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class DeployTest {
	private static final String VERSION_ID = "7959";
	private static final String DEPLOYMENT_ID = "1234";
	private static final String USER_NAME = "muleuser1";
	private static final String PASSWORD = "pwd1234";
	private static final String SERVER_GROUP = "Development";
	private static final String NAME = "MyMuleApp";
	private static final String VERSION = "1.0-SNAPSHOT";
	
    private Deploy deploy;
    
    private MuleRest mockMuleRest;

    @Before
    public void setup() throws Exception {
	deploy = spy(new Deploy());
	setupMocks();
	Log log = new SystemStreamLog();

	deploy.setLog(log);
	deploy.appDirectory = File.createTempFile("123", null);
	deploy.outputDirectory = File.createTempFile("456", null);

	deploy.finalName = "";
	deploy.muleApiUrl = new URL("http", "localhost", 8080, "");
	deploy.username = USER_NAME;
	deploy.password = PASSWORD;
	deploy.serverGroup = SERVER_GROUP;
	deploy.name = NAME;
	deploy.version = VERSION;
    }
    
    private void setupMocks() throws Exception{
	doNothing().when(deploy).validateProject(any(File.class));
	doReturn(null).when(deploy).getMuleZipFile(any(File.class), anyString());
	mockMuleRest = mock(MuleRest.class);
	when(deploy.buildMuleRest()).thenReturn(mockMuleRest);
	when(mockMuleRest.restfullyUploadRepository(anyString(), anyString(), any(File.class))).thenReturn(VERSION_ID);
	when(mockMuleRest.restfullyCreateDeployment(anyString(), anyString(), anyString())).thenReturn(DEPLOYMENT_ID);
    }

    @Test(expected = MojoFailureException.class)
    public void testUsernameNull() throws MojoExecutionException, MojoFailureException {
	deploy.username = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }

    @Test(expected = MojoFailureException.class)
    public void testPasswordNull() throws MojoExecutionException, MojoFailureException {
	deploy.password = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }

    @Test(expected = MojoFailureException.class)
    public void testOutputDirectoryNull() throws MojoExecutionException, MojoFailureException {
	deploy.outputDirectory = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }

    @Test(expected = MojoFailureException.class)
    public void testFinalNameNull() throws MojoExecutionException, MojoFailureException {
	deploy.finalName = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }

    @Test(expected = MojoFailureException.class)
    public void testServerGroupNull() throws MojoExecutionException, MojoFailureException {
	deploy.serverGroup = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }

    @Test
    public void testDeploymentNameNull() throws MojoExecutionException, MojoFailureException {
        deploy.deploymentName = null;
        deploy.execute();
        Assert.assertEquals("When null, deploymentName should be same as name", deploy.name, deploy.deploymentName);
    }
    
    @Test
    public void testHappyPath() throws Exception{
	deploy.execute();
	verify(mockMuleRest).restfullyUploadRepository(NAME, VERSION, null);
	verify(mockMuleRest).restfullyCreateDeployment(SERVER_GROUP, NAME, VERSION_ID);
	verify(mockMuleRest).restfullyDeployDeploymentById(DEPLOYMENT_ID);
    }
}