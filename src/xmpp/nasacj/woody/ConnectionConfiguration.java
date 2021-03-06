package xmpp.nasacj.woody;

import java.io.File;
import javax.net.SocketFactory;

import xmpp.nasacj.woody.proxy.ProxyInfo;
import xmpp.nasacj.woody.util.DNSUtil;

public class ConnectionConfiguration implements Cloneable
{
	/**
	 * Hostname of the XMPP server. Usually servers use the same service name as
	 * the name of the server. However, there are some servers like google where
	 * host would be talk.google.com and the serviceName would be gmail.com.
	 */
	private String serviceName;

	private String host;
	private int port;
	
	private String truststorePath;
    private String truststoreType;
    private String truststorePassword;
    private String keystorePath;
    private String keystoreType;
    private String pkcs11Library;
    private boolean verifyChainEnabled = false;
    private boolean verifyRootCAEnabled = false;
    private boolean selfSignedCertificateEnabled = false;
    private boolean expiredCertificatesCheckEnabled = false;
    private boolean notMatchingDomainCheckEnabled = false;

	protected ProxyInfo proxy;

	private boolean compressionEnabled = false;

	private boolean saslAuthenticationEnabled = true;
	
	// Holds the authentication information for future reconnections
    private String username;
    private String password;
    private String resource;
    private boolean sendPresence = true;
    private boolean rosterLoadedAtLogin = true;
    private SecurityMode securityMode = SecurityMode.enabled;

	// Holds the socket factory that is used to generate the socket in the
	// connection
	private SocketFactory socketFactory;

	/**
	 * Creates a new ConnectionConfiguration for the specified service name. A
	 * DNS SRV lookup will be performed to find out the actual host address and
	 * port to use for the connection.
	 * 
	 * @param serviceName
	 *            the name of the service provided by an XMPP server.
	 */
	public ConnectionConfiguration(String serviceName)
	{
		// Perform DNS lookup to get host and port to use
		DNSUtil.HostAddress address = DNSUtil.resolveXMPPDomain(serviceName);
		init(address.getHost(), address.getPort(), serviceName,
				ProxyInfo.forDefaultProxy());
	}

	/**
	 * Creates a new ConnectionConfiguration using the specified host, port and
	 * service name. This is useful for manually overriding the DNS SRV lookup
	 * process that's used with the {@link #ConnectionConfiguration(String)}
	 * constructor. For example, say that an XMPP server is running at localhost
	 * in an internal network on port 5222 but is configured to think that it's
	 * "example.com" for testing purposes. This constructor is necessary to
	 * connect to the server in that case since a DNS SRV lookup for example.com
	 * would not point to the local testing server.
	 * 
	 * @param host
	 *            the host where the XMPP server is running.
	 * @param port
	 *            the port where the XMPP is listening.
	 * @param serviceName
	 *            the name of the service provided by an XMPP server.
	 */
	public ConnectionConfiguration(String host, int port, String serviceName)
	{
		init(host, port, serviceName, ProxyInfo.forDefaultProxy());
	}

	/**
	 * Creates a new ConnectionConfiguration for the specified service name with
	 * specified proxy. A DNS SRV lookup will be performed to find out the
	 * actual host address and port to use for the connection.
	 * 
	 * @param serviceName
	 *            the name of the service provided by an XMPP server.
	 * @param proxy
	 *            the proxy through which XMPP is to be connected
	 */
	public ConnectionConfiguration(String serviceName, ProxyInfo proxy)
	{
		// Perform DNS lookup to get host and port to use
		DNSUtil.HostAddress address = DNSUtil.resolveXMPPDomain(serviceName);
		init(address.getHost(), address.getPort(), serviceName, proxy);
	}

	/**
	 * Creates a new ConnectionConfiguration using the specified host, port and
	 * service name. This is useful for manually overriding the DNS SRV lookup
	 * process that's used with the {@link #ConnectionConfiguration(String)}
	 * constructor. For example, say that an XMPP server is running at localhost
	 * in an internal network on port 5222 but is configured to think that it's
	 * "example.com" for testing purposes. This constructor is necessary to
	 * connect to the server in that case since a DNS SRV lookup for example.com
	 * would not point to the local testing server.
	 * 
	 * @param host
	 *            the host where the XMPP server is running.
	 * @param port
	 *            the port where the XMPP is listening.
	 * @param serviceName
	 *            the name of the service provided by an XMPP server.
	 * @param proxy
	 *            the proxy through which XMPP is to be connected
	 */
	public ConnectionConfiguration(String host, int port, String serviceName,
			ProxyInfo proxy)
	{
		init(host, port, serviceName, proxy);
	}

	/**
	 * Creates a new ConnectionConfiguration for a connection that will connect
	 * to the desired host and port.
	 * 
	 * @param host
	 *            the host where the XMPP server is running.
	 * @param port
	 *            the port where the XMPP is listening.
	 */
	public ConnectionConfiguration(String host, int port)
	{
		init(host, port, host, ProxyInfo.forDefaultProxy());
	}

	private void init(String host, int port, String serviceName, ProxyInfo proxy)
	{
		this.host = host;
		this.port = port;
		this.serviceName = serviceName;
		this.proxy = proxy;
		
		// Build the default path to the cacert truststore file. By default we are
        // going to use the file located in $JREHOME/lib/security/cacerts.
        String javaHome = System.getProperty("java.home");
        StringBuilder buffer = new StringBuilder();
        buffer.append(javaHome).append(File.separator).append("lib");
        buffer.append(File.separator).append("security");
        buffer.append(File.separator).append("cacerts");
        truststorePath = buffer.toString();
        // Set the default store type
        truststoreType = "jks";
        // Set the default password of the cacert file that is "changeit"
        truststorePassword = "changeit";
        keystorePath = System.getProperty("javax.net.ssl.keyStore");
        keystoreType = "jks";
        pkcs11Library = "pkcs11.config";
        
        //System.err.println("truststorePath=" + truststorePath);
        //System.err.println("keystorePath=" + keystorePath);
        
        //Setting the SocketFactory according to proxy supplied
        socketFactory = proxy.getSocketFactory();
	}

	/**
	 * Sets the server name, also known as XMPP domain of the target server.
	 * 
	 * @param serviceName
	 *            the XMPP domain of the target server.
	 */
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	/**
	 * Returns the server name of the target server.
	 * 
	 * @return the server name of the target server.
	 */
	public String getServiceName()
	{
		return serviceName;
	}

	/**
	 * Returns the host to use when establishing the connection. The host and
	 * port to use might have been resolved by a DNS lookup as specified by the
	 * XMPP spec (and therefore may not match the {@link #getServiceName service
	 * name}.
	 * 
	 * @return the host to use when establishing the connection.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Returns the port to use when establishing the connection. The host and
	 * port to use might have been resolved by a DNS lookup as specified by the
	 * XMPP spec.
	 * 
	 * @return the port to use when establishing the connection.
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
     * Returns the TLS security mode used when making the connection. By default,
     * the mode is {@link SecurityMode#enabled}.
     *
     * @return the security mode.
     */
    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    /**
     * Sets the TLS security mode used when making the connection. By default,
     * the mode is {@link SecurityMode#enabled}.
     *
     * @param securityMode the security mode.
     */
    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    /**
     * Retuns the path to the trust store file. The trust store file contains the root
     * certificates of several well known CAs. By default, will attempt to use the
     * the file located in $JREHOME/lib/security/cacerts.
     *
     * @return the path to the truststore file.
     */
    public String getTruststorePath() {
        return truststorePath;
    }

    /**
     * Sets the path to the trust store file. The truststore file contains the root
     * certificates of several well?known CAs. By default Smack is going to use
     * the file located in $JREHOME/lib/security/cacerts.
     *
     * @param truststorePath the path to the truststore file.
     */
    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    /**
     * Returns the trust store type, or <tt>null</tt> if it's not set.
     *
     * @return the trust store type.
     */
    public String getTruststoreType() {
        return truststoreType;
    }

    /**
     * Sets the trust store type.
     *
     * @param truststoreType the trust store type.
     */
    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    /**
     * Returns the password to use to access the trust store file. It is assumed that all
     * certificates share the same password in the trust store.
     *
     * @return the password to use to access the truststore file.
     */
    public String getTruststorePassword() {
        return truststorePassword;
    }

    /**
     * Sets the password to use to access the trust store file. It is assumed that all
     * certificates share the same password in the trust store.
     *
     * @param truststorePassword the password to use to access the truststore file.
     */
    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    /**
     * Retuns the path to the keystore file. The key store file contains the 
     * certificates that may be used to authenticate the client to the server,
     * in the event the server requests or requires it.
     *
     * @return the path to the keystore file.
     */
    public String getKeystorePath() {
        return keystorePath;
    }

    /**
     * Sets the path to the keystore file. The key store file contains the 
     * certificates that may be used to authenticate the client to the server,
     * in the event the server requests or requires it.
     *
     * @param keystorePath the path to the keystore file.
     */
    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    /**
     * Returns the keystore type, or <tt>null</tt> if it's not set.
     *
     * @return the keystore type.
     */
    public String getKeystoreType() {
        return keystoreType;
    }

    /**
     * Sets the keystore type.
     *
     * @param keystoreType the keystore type.
     */
    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }


    /**
     * Returns the PKCS11 library file location, needed when the
     * Keystore type is PKCS11.
     *
     * @return the path to the PKCS11 library file
     */
    public String getPKCS11Library() {
        return pkcs11Library;
    }

    /**
     * Sets the PKCS11 library file location, needed when the
     * Keystore type is PKCS11
     *
     * @param pkcs11Library the path to the PKCS11 library file
     */
    public void setPKCS11Library(String pkcs11Library) {
        this.pkcs11Library = pkcs11Library;
    }

    /**
     * Returns true if the whole chain of certificates presented by the server are going to
     * be checked. By default the certificate chain is not verified.
     *
     * @return true if the whole chaing of certificates presented by the server are going to
     *         be checked.
     */
    public boolean isVerifyChainEnabled() {
        return verifyChainEnabled;
    }

    /**
     * Sets if the whole chain of certificates presented by the server are going to
     * be checked. By default the certificate chain is not verified.
     *
     * @param verifyChainEnabled if the whole chaing of certificates presented by the server
     *        are going to be checked.
     */
    public void setVerifyChainEnabled(boolean verifyChainEnabled) {
        this.verifyChainEnabled = verifyChainEnabled;
    }

    /**
     * Returns true if root CA checking is going to be done. By default checking is disabled.
     *
     * @return true if root CA checking is going to be done.
     */
    public boolean isVerifyRootCAEnabled() {
        return verifyRootCAEnabled;
    }

    /**
     * Sets if root CA checking is going to be done. By default checking is disabled.
     *
     * @param verifyRootCAEnabled if root CA checking is going to be done.
     */
    public void setVerifyRootCAEnabled(boolean verifyRootCAEnabled) {
        this.verifyRootCAEnabled = verifyRootCAEnabled;
    }

    /**
     * Returns true if self-signed certificates are going to be accepted. By default
     * this option is disabled.
     *
     * @return true if self-signed certificates are going to be accepted.
     */
    public boolean isSelfSignedCertificateEnabled() {
        return selfSignedCertificateEnabled;
    }

    /**
     * Sets if self-signed certificates are going to be accepted. By default
     * this option is disabled.
     *
     * @param selfSignedCertificateEnabled if self-signed certificates are going to be accepted.
     */
    public void setSelfSignedCertificateEnabled(boolean selfSignedCertificateEnabled) {
        this.selfSignedCertificateEnabled = selfSignedCertificateEnabled;
    }

    /**
     * Returns true if certificates presented by the server are going to be checked for their
     * validity. By default certificates are not verified.
     *
     * @return true if certificates presented by the server are going to be checked for their
     *         validity.
     */
    public boolean isExpiredCertificatesCheckEnabled() {
        return expiredCertificatesCheckEnabled;
    }

    /**
     * Sets if certificates presented by the server are going to be checked for their
     * validity. By default certificates are not verified.
     *
     * @param expiredCertificatesCheckEnabled if certificates presented by the server are going
     *        to be checked for their validity.
     */
    public void setExpiredCertificatesCheckEnabled(boolean expiredCertificatesCheckEnabled) {
        this.expiredCertificatesCheckEnabled = expiredCertificatesCheckEnabled;
    }
    
    /**
     * Returns true if certificates presented by the server are going to be checked for their
     * domain. By default certificates are not verified.
     *
     * @return true if certificates presented by the server are going to be checked for their
     *         domain.
     */
    public boolean isNotMatchingDomainCheckEnabled() {
        return notMatchingDomainCheckEnabled;
    }

    /**
     * Sets if certificates presented by the server are going to be checked for their
     * domain. By default certificates are not verified.
     *
     * @param notMatchingDomainCheckEnabled if certificates presented by the server are going
     *        to be checked for their domain.
     */
    public void setNotMatchingDomainCheckEnabled(boolean notMatchingDomainCheckEnabled) {
        this.notMatchingDomainCheckEnabled = notMatchingDomainCheckEnabled;
    }

	/**
	 * Returns true if the connection is going to use stream compression. Stream
	 * compression will be requested after TLS was established (if TLS was
	 * enabled) and only if the server offered stream compression. With stream
	 * compression network traffic can be reduced up to 90%. By default
	 * compression is disabled.
	 * 
	 * @return true if the connection is going to use stream compression.
	 */
	public boolean isCompressionEnabled()
	{
		return compressionEnabled;
	}

	/**
	 * Sets if the connection is going to use stream compression. Stream
	 * compression will be requested after TLS was established (if TLS was
	 * enabled) and only if the server offered stream compression. With stream
	 * compression network traffic can be reduced up to 90%. By default
	 * compression is disabled.
	 * 
	 * @param compressionEnabled
	 *            if the connection is going to use stream compression.
	 */
	public void setCompressionEnabled(boolean compressionEnabled)
	{
		this.compressionEnabled = compressionEnabled;
	}

	/**
	 * Returns true if the client is going to use SASL authentication when
	 * logging into the server. If SASL authenticatin fails then the client will
	 * try to use non-sasl authentication. By default SASL is enabled.
	 * 
	 * @return true if the client is going to use SASL authentication when
	 *         logging into the server.
	 */
	public boolean isSASLAuthenticationEnabled()
	{
		return saslAuthenticationEnabled;
	}

	/**
	 * Sets whether the client will use SASL authentication when logging into
	 * the server. If SASL authenticatin fails then the client will try to use
	 * non-sasl authentication. By default, SASL is enabled.
	 * 
	 * @param saslAuthenticationEnabled
	 *            if the client is going to use SASL authentication when logging
	 *            into the server.
	 */
	public void setSASLAuthenticationEnabled(boolean saslAuthenticationEnabled)
	{
		this.saslAuthenticationEnabled = saslAuthenticationEnabled;
	}

	/**
	 * Returns the socket factory used to create new xmppConnection sockets.
	 * This is useful when connecting through SOCKS5 proxies.
	 * 
	 * @return socketFactory used to create new sockets.
	 */
	public SocketFactory getSocketFactory()
	{
		return this.socketFactory;
	}

	/**
	 * Sets the socket factory used to create new xmppConnection sockets. This
	 * is useful when connecting through SOCKS5 proxies.
	 * 
	 * @param socketFactory
	 *            used to create new sockets.
	 */
	public void setSocketFactory(SocketFactory socketFactory)
	{
		this.socketFactory = socketFactory;
	}
	
	/**
     * An enumeration for TLS security modes that are available when making a connection
     * to the XMPP server.
     */
    public static enum SecurityMode {

        /**
         * Securirty via TLS encryption is required in order to connect. If the server
         * does not offer TLS or if the TLS negotiaton fails, the connection to the server
         * will fail.
         */
        required,

        /**
         * Security via TLS encryption is used whenever it's available. This is the
         * default setting.
         */
        enabled,

        /**
         * Security via TLS encryption is disabled and only un-encrypted connections will
         * be used. If only TLS encryption is available from the server, the connection
         * will fail.
         */
        disabled
    }
}
