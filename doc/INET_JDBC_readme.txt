SPRINTA JDBC 2.0 Driver Version 1.03 for MS SQL Server
Last Modified:  20-Oct-1999


Table Of Contents
-----------------

	1    Changes To Previous Versions
	1.1  Changes To Version 1.00
	1.2  Changes To Version 1.01
	1.3  Changes To Version 1.02
	2    Installation
	3    Getting Started
	3.1  SQL Server, Java and JDBC Versions
	3.2  Check Host Name And Port Number Of Your Server
	3.3  Driver Name
	3.4  URL Syntax
	3.5  Implemented Properties
	3.6  Connection Example
	4.   Named Pipes
	5.   Escape Clauses
	5.1  Date and Time
	5.2  Stored Procedures
	5.3  Functions
	6.   Character Converting 
	7.   New Datatypes with SQL Server 7.0
	8.   Scrolling Cursor Types
	9.   Locking and concurrency control
	10.  Copyright and Support




1 Changes To Previous Versions
------------------------------
1.1  Changes To Version 1.00
----------------------------
	- A bug with getSchemas() was fixed.
	- The coding from getUnicodeStream() was changed.
	- A bug with updateable PreparedStatement and CallableStatement was fixed.
	- Pessimistic Locking was added.
	- After an insertRow() the ResultSet shows the new row.
	- The functions with getConcurrency() and getType() now return the correct value.
	- Scrolling problems with TYPE_FORWARD_ONLY AND CONCUR_UPDATABLE were fixed.
	- Dynamic Cursors were added.
	- The exception from setFetchDirection() and getFetchDirection() was removed.



1.2  Changes To Version 1.01
----------------------------
	- A bug with host names larger 30 character was fixed.
	- Values from tinyint columns return in the range [0,255] and not in the
	  range [-128,127].
	- Many functions of DatabaseMetaData were implemented.
	- The NullPointerException with updateNull() was fixed.
	- A bug with wasNull() was fixed.
	- Some Bugs with position-reporting methods were fixed.
	- The method setCursorName() was implemented.
	- A threading problem was fixed.


1.3  Changes To Version 1.02
----------------------------
	- A bug with updateNull() and SQL Server 6.5 was fixed.
	- The function updateBinaryStream() was implemented.
	- Problems with refresh after delete were fixed.


2 Installation
----------------
	- copy the class files in your classpath
List of the files:
	- com.inet.tds.TdsDriver
	- com.inet.tds.TdsConnection
	- com.inet.tds.TdsResultSet
	- com.inet.tds.TdsStatement
	- com.inet.tds.TdsResultSetMetaData
	- com.inet.tds.TdsDatabaseMetaData
	- com.inet.tds.SqlFunctions		only for use with Escape Functions
	- com.inet.tds.Lob




3   Getting Started
--------------------

3.1 SQL Server, Java and JDBC Versions
--------------------------------------
	Java Versions: 1.2x
	JDBC Version:  2.0
	SQL Server Version:
	- Microsoft SQL Server 7.0
	- Microsoft SQL Server 6.5



3.2 Check Host Name And Port Number Of Your Server
--------------------------------------------------
This driver works with Microsoft SQL Servers that are configured to use
the TCP/IP networking protocol. Please verify that your server is
currently listening on a TCP/IP port. 

If you know  that your SQL Server is listening on a TCP/IP port and you
know the host name of your SQL Server and the port number you can go to
the next chapter.

To check or enable the TCP/IP Sockets protocol follow these steps:
For Microsoft SQL Server 6.5: 
Click -SQL Setup- in the MS SQL Server program group.
If not selected select -Change Network Support-, select -TCP/IP- and enter 
the port number you want to use (default port: 1433).
If -Change Network Support- is selected, than cancel the setup.
  
For Microsoft SQL Server 7.0: 
Click -SQL Server Network Utility- in the Microsoft SQL Server 7.0 program group. 
On the general property sheet, click -Add- and select -TCP/IP- under Network libraries.
Enter the port number and the proxy address (if nesessary) and click OK.
 
The default port number for the Microsoft SQL Server is usually 1433.
However servers can be configured to listen on any port number.
  
To make sure that the RDBMS server is listening on the machine name and
port number you specified use: 
  
telnet <hostname or ip address> <port number> 

If the connection is refused, then the hostname or the port number are
incorrect. 



3.3 Driver Name
---------------
The class name of the driver is
	com.inet.tds.TdsDriver



3.4 Url Syntax
--------------
	jdbc:inetdae:hostname:portnumber
	jdbc:inetdae:hostname			-> with default port 1433
	jdbc:inetdae:hostname:portnumber?database=MyDb&language=deutsch
						-> with properties
	jdbc:inetdae://servername/pipe/pipename	-> with named pipes

e.g.	jdbc:inetdae:www.inetsoftware.de:1433
	jdbc:inetdae:localHost:1433
	jdbc:inetdae://MyServer/pipe/sql/query



3.5 Implemented Properties
--------------------------
	- database	-> default is "master"
	- language	-> default is "us_english"
			-> "" -> SQL Server default language
	- user
	- password
	- charset	-> see Character Converting
	- nowarnings	-> "true" getWarnings() returns null
	- sql7		-> default is "false" 
			-> "true" the new datatypes supported


There are two ways to put the properties to the driver:
	1. append the properties to the URL like this
		jdbc:inetdae:hostname:portnumber?database=MyDb&language=deutsch

	2. call from method getConnection(String url, Properties info) from
	   the driver manager.



3.6 Connection Example
----------------------
import java.sql.*;                  // JDBC package

String url = "jdbc:inetdae:localhost:1433";	// use your hostname and port number here
String login = "sa";	   			// use your login here
String password = "";	   			// use your password here

	try{
		DriverManager.setLogStream(System.out); // to create more info 
							  // for technical support
			
		//load the class with the driver
		//Class.forName("com.inet.tds.TdsDriver");		// JDK,Netscape
		//or
		Class.forName("com.inet.tds.TdsDriver").newInstance();	// JDK,Netscape,IE
		//or
		//new com.inet.tds.TdsDriver();			// JDK,Netscape,IE

		
		//set a timeout for login and query
		DriverManager.setLoginTimeout(10);


		//open a connection to the database
		Connection connection = DriverManager.getConnection(url,login,password);

		//to get the driver version
	        DatabaseMetaData conMD = connection.getMetaData();
	        System.out.println("Driver Name:\t"    + conMD.getDriverName());
	        System.out.println("Driver Version:\t" + conMD.getDriverVersion());

		//select a database
		connection.setCatalog( "MyDatabase");

		//create a statement
		Statement st = connection.createStatement();

		//execute a query
		ResultSet rs = st.executeQuery("SELECT * FROM tblExample");

		// read the data and put it to the console
		while (rs.next()){
			for(int j=1; j<=rs.getMetaData().getColumnCount(); j++){
				System.out.print( rs.getObject(j)+"\t");
			}
			System.out.println();    
		}

		
		//close the objects
		st.close();
		connection.close();

	}catch(Exception e){
		e.printStackTrace();
	}




4. Named Pipes
--------------
Another solution to connect to the sql server are named pipes.
Named pipes are working only in the Java VM 1.1.7 or higher and 
the Java VM 1.2Beta 4 or higher. Named pipes are equal to files 
with UNC path. We have tested named pipes only with the Win32 VM
from Sun. If you want to use named pipes from another platform,  
you have to install SMB (server message block) on the client or you must 
install NFS (network file system) on the sql server.

The default pipe of the sql server is "/sql/query" but you
can change this pipe name in the server manager.




5.   Escape Clauses
-------------------
The driver implements follow escape clauses:

5.1  Date and Time
------------------
	{d 'yyyy-mm-dd'}
	{t 'hh:mm:ss[.fff]'}
	{ts 'yyyy-mm-dd hh:mm:ss[.fff]'}

5.2  Stored Procedures
----------------------
	{call storedProcedures('Param1'[,'Param2'][,?][...])}
	{? = call storedProcedures('Param1'[,'Param2'][,?][...])}

5.3  Functions
--------------
	{fn now()}
	{fn curdate()}
	{fn curtime()}





6.   Character Converting 
-------------------------
By default character converting is disabled. To use character converting in the driver you need to append "charset=YourCharSet" to the url.

for example: "jdbc:inetdae:localhost:1433?charset=Cp1250"
or           "jdbc:inetdae:localhost:1433?charset=" + sun.io.ByteToCharConverter.getDefault().getCharacterEncoding();
or           "jdbc:inetdae:localhost:1433?charset=" + System.getProperty("file.encoding");
or           "jdbc:inetdae:localhost:1433?charset=" + (new java.io.InputStreamReader(in)).getEncoding();

TIP: The property charset is case-sensitive in JAVA. The name of the character set is also
case-sensitive.

You can test the availability of a charachter set with:
"test string".getBytes( charset );
If this line executes correctly the charachter set is available in the current VM.

The parameter charset is ignoring if you use the option sql7=true. To save 
national characters with sql7=true you need to use the character N. 
Example: "INSERT INTO myTable(ntext field) VALUES( N'national text' )"





7.   New Datatypes with SQL Server 7.0
--------------------------------------
The SQL server 7.0 supports new datatypes, i.e. nchar, ntext, nvarchar, 
varchar larger than 255 character.

You can use the new datatypes if you set the property sql7=true. 

If you set the property sql7=true you will not be able to connect to the SQL Server 6.5.




8. Scrolling Cursor Types
-------------------------

id                      |                | Description
------------------------|----------------|--------------------------------------------------
TYPE_FORWARD_ONLY       | Forward-only,  | The fetch functions will allow only a fetchtype of 
                        | dynamic cursor | FIRST, NEXT, or RELATIVE with a positive rownum.
------------------------|----------------|--------------------------------------------------
TYPE_SCROLL_INSENSITIVE | Insensitive    | Use a concurrency of CONCUR_READ_ONLY. 
                        | keyset         | SQL Server will generate a temporary table, so
                        | cursor         | changes made to the rows by others will not be
                        |                | visible through the cursor.
                        |                | The fetch functions will allow all fetchtype
                        |                | values. 
------------------------|----------------|--------------------------------------------------
TYPE_SCROLL_SENSITIVE   | Keyset cursor  | The fetch functions will allow all fetchtype
                        |                | values.
------------------------|----------------|--------------------------------------------------
TYPE_SCROLL_SENSITIVE+1 | Dynamic cursor | The fetch functions will allow all fetchtype
                        |                | values except RANDOM. All position-reporting
                        |                | methods returns always false.





9.   Locking and concurrency control
------------------------------------
Concurrency type is one of the following concurrency control options. 


concurrency type	|	Description 
------------------------|-------------------------------------------------------------------
CONCUR_READ_ONLY	| Read-only cursor. You cannot modify rows in the cursor result set.
------------------------|-------------------------------------------------------------------
CONCUR_UPDATABLE	| Optimistic concurrency control using timestamp or values. Changes 
			| to a row that are initiated through the cursor succeed only if the 
			| row remains unchanged since the last fetch. Changes are detected by 
			| comparing timestamps or by comparing all nontext, nonimage values 
			| if timestamps are not available. 
------------------------|-------------------------------------------------------------------
CONCUR_UPDATABLE+1	| Intent to update locking. Places an update intent lock on the data 
			| page that contains each row as it is fetched. If not inside an open 
			| transaction, the locks are released when the next fetch is performed. 
			| If inside an open transaction, the locks are released when the 
			| transaction is closed.
------------------------|-------------------------------------------------------------------
CONCUR_UPDATABLE+2	| Optimistic concurrency control using values. Changes to a row through 
			| the cursor succeed only if the row remains unchanged since the last 
			| fetch. Changes are detected by comparing all nontext, nonimage values. 







10.   Copyright and Support
---------------------------
	Copyright by i-net software
	More info and updates are located at
	http://www.inetsoftware.de
	news://news.inetsoftware.de




© 1998/1999 i-net software

