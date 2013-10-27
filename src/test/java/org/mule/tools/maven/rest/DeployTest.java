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

public class DeployTest {
    private Deploy deploy;

    @Before
    public void setup() throws Exception {
	deploy = new Deploy();
	Log log = new SystemStreamLog();

	deploy.setLog(log);
	deploy.appDirectory = File.createTempFile("123", null);
	deploy.outputDirectory = File.createTempFile("456", null);

	deploy.finalName = "";
	deploy.muleApiUrl = new URL("http", "localhost", 8080, "");
	deploy.username = "";
	deploy.password = "";
	deploy.serverGroup = "";
	deploy.name = "";
	deploy.version = "";
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
    public void testServerGRoupNull() throws MojoExecutionException, MojoFailureException {
	deploy.serverGroup = null;
	deploy.execute();
	Assert.fail("Exception should have been thrown before this is called");
    }
}