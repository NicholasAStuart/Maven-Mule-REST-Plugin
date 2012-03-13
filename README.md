#Maven Mule REST Plugin#

This is a project to utilize the RESTful interface that is provided for the Management console on Mule ESB's Enterprise Edition. 

This project makes heavy use of the already existing [maven-mule-plugin](https://github.com/mulesoft/maven-mule-plugin). Use of their archiving, dependency management were used as it was available to do a lot of the legwork with it's existing code.

This is a personal project and is not affiliated with MuleSoft or the maven mule plugin in any way.

Example:

	<project>
		...
		<build>
			<plugins>
				<plugin>
					<groupId>org.neuralsandbox</groupId>
					<artifactId>mule-rest</artifactId>
					<version>1.0.0-SNAPSHOT</version>
				</plugin>
			</plugins>
		</build>
		...
	</project>

# Calling the plugin #

There is only one goal, deploy. To call the plugin, do the following

	mule-rest:deploy

## Security ##
In order to post to the Mule Repository, you need only these permissions:

*	Repository Read 
*	Repository Modify

## Configuration Options ##
<table>
	<tr>
		<th>Property
		<th>Description
		<th>Default
<tr>
	<td>
		muleRepositoryUrl
	<td>
		The URL of the Mule MMC, with the path to the repository(usually .../api/repository)
	<td>
		http://localhost:8585/mmc/api/repository
<tr>
	<td>
		name
	<td>
		What to name the application when it is uploaded to the repository
	<td>
		MuleApplication
<tr>
	<td>
		version
	<td>
		What version to give the software when it is uploaded to the repository
	<td>
		Current Time, in MM-dd-yyyy HH:mm:ss format
<tr>
	<td>
		password
	<td>
		The password to the Mule Repository.
<tr>
	<td>
		username
	<td>
		The username to the Mule Repository.
	<td>
		admin
</table> 