#Maven Mule REST Plugin#

This is a project to utilize the RESTful interface that is provided for the Management console on Mule ESB's Enterprise Edition. 

This plugin assumes that you have configured the [maven-mule-plugin](https://github.com/mulesoft/maven-mule-plugin) to generated the mule application archive

This is a personal project and is not affiliated with MuleSoft or the maven mule plugin in any way.

Example:

	<project>
		...
		<build>
			<plugins>
				<plugin>
					<groupId>org.redmage</groupId>
					<artifactId>mule-mmc-rest-plugin</artifactId>
					<version>1.1.0</version>
				</plugin>
			</plugins>
		</build>
		...
	</project>

# Calling the plugin #

There is only one goal, deploy. To call the plugin, do the following

	mule-mmc-rest-plugin:deploy
	
This goal will
*	upload the mule application archive to the MMC Repository
*	delete an existing deployment having the same application name
*	create a new deployment this the uploaded archive, with target the given serverGroup
*	perform a deploy request to make MMC deploy into target server group

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
		muleApiUrl
	<td>
		The URL of the Mule MMC API (usually .../api)
	<td>
		http://localhost:8585/mmc/api
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
		serverGroup
	<td>
		The name of the target Mule serverGroup
	<td>
<tr>
	<td>
		password
	<td>
		The password to the Mule MMC API.
	<td>
<tr>
	<td>
		username
	<td>
		The username to the Mule MMC API.
	<td>
</table> 
