package org.mule.tools.maven.rest;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * @goal deploy
 * @execute phase="compile"
 * @requiresDirectInvocation true
 * @requiresDependencyResolution runtime
 * 
 * @author Nicholas A. Stuart
 * @author Mohamed EL HABIB
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
	StaticLoggerBinder.getSingleton()
		.setLog(getLog());
	Logger logger = LoggerFactory.getLogger(getClass());

	if (name == null) {
	    logger.info("Name is not set, using default \"{}\"", DEFAULT_NAME);
	    name = DEFAULT_NAME;
	}
	if (version == null) {
	    version = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance()
		    .getTime());
	    logger.info("Version is not set, using a default of the timestamp: {}", version);
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
	    validateProject(appDirectory);
	    MuleRest muleRest = new MuleRest(muleApiUrl, username, password);
	    String versionId = muleRest.restfullyUploadRepository(name, version, getMuleZipFile(outputDirectory, finalName));
	    String deploymentId = muleRest.restfullyCreateDeployment(serverGroup, name, versionId);
	    muleRest.restfullyDeployDeploymentById(deploymentId);
	} catch (Exception e) {
	    throw new MojoFailureException("Error in attempting to deploy archive: " + e.toString(), e);
	}
    }

    private static final File getMuleZipFile(File outputDirectory, String filename) throws MojoFailureException {
	File file = new File(outputDirectory, filename + ".zip");
	if (!file.exists()) {
	    throw new MojoFailureException("There no application ZIP file generated : check that you have configured the maven-mule-plugin to generated the this file");
	}
	return file;
    }

    private static final void validateProject(File appDirectory) throws MojoExecutionException {
	File muleConfig = new File(appDirectory, "mule-config.xml");
	File deploymentDescriptor = new File(appDirectory, "mule-deploy.properties");

	if ((muleConfig.exists() == false) && (deploymentDescriptor.exists() == false)) {
	    throw new MojoExecutionException("No mule-config.xml or mule-deploy.properties");
	}
    }

}