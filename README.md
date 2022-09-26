## AWS Credentials

This is an application that is run as a background process to monitor for fresh downloads of a set of credentials to a file.
If a download is detected, the new credentials are analyzed for a match among a known set of named profiles.
If a match is found, the corresponding named profile in the ~/.aws/credentials file is updated/added.

#### Scenario:

Your organization restricts IAM principal users in favor of leveraging its own corporate user directory, i.e. Microsoft Active Directory.
You essentially assume an IAM role as visitor, but there is no IAM principal with its own  persistent [IAM access key](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys). This is implemented via [SAML federated access](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_enable-console-saml.html). 

> "*This operation provides a mechanism for tying an enterprise identity store or directory to role-based AWS access without user-specific credentials or configuration*"

#### Problem:

You are a developer in need of CLI access, which means you need a valid set of credentials

- aws_access_key_id
- aws_secret_access_key
- aws_session_token

How do you get cli access to the aws account without an IAM principal and its [IAM access key](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys) ?

#### One Solution:

With [SAML 2.0 federated users to access](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_enable-console-saml.html), once having authenticated through your organizations IDP, one would be redirected either:

- Directly to the aws management console.
  or...
- Indirectly to the aws management console via a "portal" page that requires a selection be made between mapped aws accounts and roles.

In either event, your organizations IDP first logs into the aws management console on your behalf and then redirects your browser to the default console page along with a base64 encoded SAML authentication response.
If you could get your hands on the SAML assertion from this response, it would be accepted by the [aws security token service (STS)](https://docs.aws.amazon.com/STS/latest/APIReference/welcome.html) to acquire a set of corresponding [security credentials including a temporary session token](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp_use-resources.html), which you could use for CLI access.

IMPLEMENTATION:

1. **Part 1: Plugin**
   The [SAML to AWS STS Keys Conversion tool](https://github.com/prolane/samltoawsstskeys/blob/master/README.md) performs this solution:

   - [Chrome plugin](https://chrome.google.com/webstore/detail/saml-to-aws-sts-keys-conv/ekniobabpcnfjgfbphhcolcinmnbehde)

   - [Firefox extension](https://addons.mozilla.org/en-US/firefox/addon/saml-to-aws-sts-keys/)

   With this plugin installed, your IDP authentication response will contain the SAML assertion, which will be detected by the plugin which uses the [STS assumeRoleWithSAML API](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRoleWithSAML.html) to acquire a set of temporary credentials and saves them off to a file in your downloads folder.

2. **Part 2: This java application**

   - **Purpose:**
     The chrome plugin comes with an optional configuration feature to map profiles to aws role ARNs. However, this does not seem to work.
     Also, if you configured the cli to look for its credentials file in your browsers download folder, it could never contain anything but a single default set of credentials. This is because the plugin completely overwrites the file it is downloading (does not append).
     Therefore, this application is run as a background process to monitor for changes to the downloaded credentials file.
     When a new download is detected, the credentials are used in a cli call:

     ```
     aws sts get-caller-identity
     ```

     This call returns the aws account number. The application then looks up the account number in a mapping properties file, supplied by you, for the corresponding profile name. The properties file would contain content like this:

     ```
     770203350335 = infnprd
     115619461932 = infprd
     730096353738 = legacy
     253997709890 = vdsnprd
     586030668166 = vdsprd
     ```

     Once the profile name is obtained, the credentials are "injected" into the `~/.aws/credentials` file to replace the corresponding profile, or add to it if it does not already exist. This is merely a time saver - keeps you from having to do this yourself manually, which gets tiresome quickly, especially if your session token has a short duration and you need access to multiple accounts frequently.

   - **Build:**
     This is a maven application and is built as an executable jar using the maven shade plugin *(no classpath dependencies need to be set - all are self contained)*. Download this repository and open a command prompt to the root folder. Execute the following command:

     ```
     mvn clean package
     or...
     mvn clean package -DskipTests=true
     ```

     The build artifact will be at `target\aws-credentials.jar`

   - **Run in background:**

     - **Windows 10**
       It is not necessary to go through the hassle of having the jar run as a windows service.
       It will suffice to place a .bat file to run it in your startup folder. You can get the location of your startup folder using the run command:

       ```
       Run shell:startup
       ```

        The contents of the .bat file would look something like this:

       ```
       java -Xmx200m -jar ~\AwsCredential\target\aws-credentials.jar ^
         source=C:\Users\wrh\Downloads\credentials ^
         target=C:\Users\wrh\.aws\credentials ^
         map=C:\Users\wrh\NamedProfileMap.properties
       ```

       Note, the arguments are name=value pairs:

       - source: The file that the browser plugin downloads STS credentials to
       - target: The standard location for the aws cli to look for credentials. This argument is optional if it is the standard location: `~/.aws/credentials`
       - map: The location of the properties file that you create to pair up aws account numbers with profile names for credentials that gain access to the corresponding accounts. *NOTE: This means the app has a limitation: only one profile per aws account.*

       This will put up a command line window that displays output. You can either minimize this window and forget about it, or use javaw instead of java. If you use javaw, you would see no window and stopping the app would require the task manager.

     - **Mac & Linux**
       Put a bash script in ~/.bash_profile or ~/.bashrc

       ```
       nohup java -Xmx200m -jar ~/AwsCredential/target/aws-credentials.jar \
         source=/home/wrh/place/where/chrome/downloads/credentials \
         target=/home/wrh/.aws/credentials \
         map=/home/wrh/NamedProfileMap.properties > /dev/null 2>&1 &
       ```

       