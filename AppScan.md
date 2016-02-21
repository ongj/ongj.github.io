---
layout: post
title: AppScan Dynamic Analyzer
permalink: /appscan/
---

##### **[Download PPT Presentation](https://github.com/ongj/appscan/raw/master/AppScan%20Dynamic%20Analyzer%20(1).ppt)**

##### **[Github Source Code](https://github.com/ongj/ongj.github.io/tree/master/Tutorial)**

===================

AppScan Dynamic Analyzer identifies security issues in web applications and helps keep them secure. It provides a downloadable report which contains the detected vulnerabilities.

In this tutorial, you will learn how to use AppScan Dynamic Analyzer to scan web applications for vulnerabilities. To further realize the importance of this service, you will first try out the vulnerabilities in the web application.

----------
##### **Download the Web Application** 
Download a copy of the web application (AppScan.war) that you will deploy in your Bluemix account.

 1. Create a directory `appscan` in the root directory.
 2. Download [AppScan.war](https://github.com/ongj/appscan/raw/master/build/libs/AppScan.war) and save it in `appscan` directory.

----------

##### **Deploy the Web Application in Bluemix using the `cf` tool** 
 1. Open a terminal window and go to `appscan` directory.

 2. Login to your Bluemix account using the `cf` tool. You will be asked for your credentials, enter your e-mail address and password of your Bluemix account.
> `cf login -a https://api.ng.bluemix.net -s dev`

 3. Upload the web application to your Bluemix account.
 > `cf push appscan-<your_name> -m 256M -p AppScan.war`

	**Example:**
> `cf push appscan-ong -m 256M -p AppScan.war`

 4. After uploading, the application you pushed will be available under the `Applications` section in the Dashboard.

----------

##### **Add and Bind Services to Web Application** 

This web application uses 2 services, PostgreSQL and AppScan Dynamic Analyzer. 

 1. In the Applications section, click the widget of your application `appscan-<your name>`.
 2. Click `ADD A SERVICE OR API`. This will redirect you to the `CATALOG` page.
> **PostgreSQL**
> 
> 1. Scroll to the bottom of the `CATALOG` page, and click `Bluemix Labs Catalog`.
> 
> 2.  Look for `postgresql` service and click it.
>
> 3.  Change the service name to any name you want and click `CREATE`.
> 
>
> **AppScan Dynamic Analyzer** (skip, service can't be added as of February 4, 2016)
> 
> 1. In the `CATALOG` page, look for `AppScan Dynamic Analyzer` service and click it.
>
> 2. Change the service name to any name you want and click `CREATE`.
> 
> ##### **NOTE:** #####
> After you add a service to the application, you will be asked if you want to restage the application. You can cancel the first notification and restage the application after you have added the 2 services mentioned.

 3. Restage the web application.

----------
##### **Understanding the Communication between the Web Application and the Services** 

As mentioned earlier, the web application uses 2 services, PostgreSQL and AppScan Dynamic Analyzer.

For the PostgreSQL service, the web application uses this service to save the accounts in a database. Initially, when the web application is first launched, the database is programmatically created and 2 rows or accounts are automatically inserted to be used for this tutorial.

If you extract the contents of the `AppScan.war` file, inside the `WEB-INF/classes/Servlet` directory, you will see the `PostgreSQLClient.java`. 

The `PostgreSQLClient.java` contains the initialization process, which includes the `getConnection()` method and the `createTable()` method. The `getConnection` method simply enables the web application to use the credentials to gain connection to the database. The `createTable` method programmatically creates the `Account` table, and inserts the 2 accounts. The `Account` table consists of 4 columns, `username`, `password`, `firstname`, and `lastname`. 

    public void createTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS Account "
                + "(username varchar(45) NOT NULL PRIMARY KEY, "
                + "password varchar(45) NOT NULL, "
                + "firstname varchar(45), "
                + "lastname varchar(45))"
                + ";";
        String sql2 = "INSERT INTO Account "
                + "(username, password, firstname, lastname) "
                + "VALUES ('admin', 'password', 'Jarrette', 'Ong'), "
                + "('user1', 'password', 'Lance', 'Del Valle');";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement = connection.prepareStatement(sql2);
            statement.executeUpdate();
        } catch (Exception e){
            System.out.println("Initialization already completed.");
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }

For the AppScan Dynamic Analyzer, it does not necessarily have a direct communication with the web application. The web application is bound with AppScan Dynamic Analyzer so that the service will be able to scan the web application for vulnerabilities. Later in this tutorial, you will see how this service scans for vulnerabilities and produces a downloadable report.

----------
##### **Launch the Web Application** 

 1. Go to the `DASHBOARD` of your Bluemix account. On the `Applications` section, click on the application `(appscan-<your_name>)` you pushed earlier.
 
 2. There is a `Route` or URL just below your application name `(appscan-<your_name>.mybluemix.net`. This will open up the web application.

> **IMPORTANT**
> 
> By entering just the Route given, you will encounter an error message:
> 
> `Error 404: SRVE0190E: File not found: /`
> 
> This is because it cannot find what page to open. In this tutorial, you will start the web application with `Login.jsp`. So `Login.jsp` will be added at the end of the URL.
> 
> New URL becomes:
> `appscan-<your_name>.mybluemix.net/Login.jsp`

----------
##### **Test the Web Application** 

The web application starts with a login page. You may try out some of the functionalities. (e.g. Login, edit name) 

As explained earlier, there are 2 accounts inserted automatically, together with the creation of the database. The 2 accounts created earlier and their user credentials (i.e username, password) are as follows.

> Username: `admin` Password: `password`
>
> Username: `user1` Password: `password`

 1. Login with the `admin` account. Credentials are (`admin`,`password`). 
 2. After a successful login, you will be redirected to the home page. there are 2 links, `Edit Name` and `Logout`. Click on `Edit Name`.
 3. Edit both first and last names to yours and click `Edit`.
 4. You will be redirected back to the home page with the new name reflected on the home page.
 5. Click `Logout`.

You have tested the functionalities like logging in and editing the name in the web application. It just looks like a simple login application. But without knowing, the web application is vulnerable to SQL Injection and Cross Site Scripting attacks.

----------
#####**Exploiting the Vulnerabilities in the Web Application** 

You will start at the login page once again after logging out. On this login page, this tutorial will demonstrate SQL Injection.

 1. Login with the `user1` account. But right now, to demonstrate SQL Injection, you will not need the password to login. Enter this as the username:

	> `user1' --`

	This is known as SQL Injection Login Bypass. `--` represents commenting out the SQL query after the `--`. The SQL query will become:

	    SELECT * FROM Account WHERE username = 'user1' -- and password = '" + pass + "';

	With the password commented out, the `WHERE` clause returns `TRUE`. Hence, you will be logged in with `user1`'s account. 

 2. On the home page, click `Edit Name`.
 3. On this edit page, this tutorial will demonstrate Cross Site Scripting or XSS attack.
 4. Enter your first name in the `First Name` field.
 5. Enter this in the `Last Name` field.

	> `<script>alert('This is an XSS attack.')</script>`

 6. You will be redirected back to the home page with an alert message, `This is an XSS attack.` This means the web application is vulnerable to XSS since it allows scripts to execute.

> ##### **Note** 
> 
> The script statement `<script>alert('This is an XSS attack.')</script>` is stored in the database. So whenever `user1` logs in the account, an alert message will appear. 
> 
> XSS attack becomes dangerous when the script entered redirects you to a malicious website and executes scripts to get user credentials. In this tutorial, it just showed you an XSS-vulnerable website.

----------
##### **Using the AppScan Dynamic Analyzer Service** (skip, service can't be added as of February 4, 2016)

 1. Click on the widget of AppScan Dynamic Analyzer under `Services` section.
 
 2. On the `"What URL should we scan?"` page, select the URL of the application. (i.e. `appscan-<your_name>.mybluemix.net`) and choose `Production Site`.
 
 3. On the `"Name your Scan"` page, create a name (e.g. `MyAppScan`) for your scan. Choose `Yes` that the application requires users to login.
 
	> If the application requires logging in, enter a working username and password in that application for testing purposes of AppScan Dynamic Analyzer service.

 4. Click `Scan` to start the scan. Scanning time ranges from minutes to days depending on the application size. For the web application used in this tutorial, scan time is around 10 to 15 minutes.
 
 5. You will be able to download the scan report as a PDF file. Here is the sample scan report for the web application used in this tutorial. Link: [appscan-jarrette.mybluemix.net.pdf](https://github.com/ongj/appscan/raw/master/appscan-jarrette.mybluemix.net.pdf)


----------


#### **End of Tutorial**

