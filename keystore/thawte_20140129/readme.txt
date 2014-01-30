
Issued 2014/01/29 from Thawte
Order:  USVIAOX1 
Validity: 29-Jan-2014 - 29-Jan-2015

Generate new viaoa.jks
    use build.xml to generate viaoa.jks
    create CSR to send to thawte.com
        keytool -certreq -alias viaoa -file viaoa.csr -keystore viaoa.jks -storepass vince1
    go to thawte and buy code signing cert
    import cert
        keytool -import -trustcacerts -keystore viaoa.jks -alias viaoa -storepass vince1 -file thawte.crt
        
    verify:
        keytool -printcert -file thawte.crt
        keytool -list -v -keystore viaoa.jks -storepass vince1


Thawte:  https://search.thawte.com/support/ssl-digital-certificates/index?page=content&id=AR185&actp=search&viewlocale=en_US
