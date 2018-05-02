# Description
This application highlights the usage of the [Spring security framework](http://google.fr) to secure a JEE Rest API Application.

It is an example usage on _how to configure Spring security to provide your own Authentication strategy_.

The _spring security_ configuration is given in both types _Java config type_ - package `net.michir.config` - and _XML namespace config type_ - file `/resources/my-security.xml` - 
(I personally prefer the namespace type as it is less verbose - security is a cross cutting concern and should be kept so in my sense!).

## Spring security in (not so) few words

1. **Custom Servlet Filter** (see _MyFilterBean_) All begins with a custom input request _Filter_ implementation, in Spring security, you would rather
 extend the _GenericFilterBean_. In this class, analyse the request, check for your custom header, token or else within the request
 and, if it meets your requirements, fire an authentication using your custom _Authentication_ implementation (see _MyAuthentication_ class)
 as below
 
 ```
     String header = request.getHeader("Authorization");
     if (!StringUtils.isEmpty(header) && header.startsWith("Token ")) {
        // ... 
        MyAuthentication myAuthentication = new MyAuthentication();
        myAuthentication.setToken(token);
 
        Authentication authentication = authenticationManager.authenticate(myAuthentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
     }
 ```
 
 if authentication fails, the _AuthenticationManager_ throws a _AuthenticaitonException_
 
 ```
    } catch (AuthenticationException ex) {
         SecurityContextHolder.clearContext();
    }
 ```
 
 Don't forget to always continue the filter chain processing (as in regular JEE Servlet based application) - GenericFilterBean
 is nothing but a _javax.servlet.Filter_ abstract implementation
 
 ```
    chain.doFilter(request, response);
    return;
 ```
 
 This filter is registered to the _Spring Security Filter chain_ as follows
 
 ```
 @EnableWebSecurity
 public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // to be pre-processed by Spring IOC (for AuthenticationManager injection)
    @Autowired
    MyFilterBean myFilterBean;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterAfter(myFilterBean, BasicAuthenticationFilter.class)        
    }
    
    @Bean
    public MyFilterBean myFilterBean() {
        return new MyFilterBean();
    }
 ```
 
 or in XML
 
 ```
     <http pattern="/**" auto-config='false' create-session="stateless"
            entry-point-ref="entryPoint" use-expressions="true">
      
            <custom-filter ref="myFilter" after="BASIC_AUTH_FILTER" />
     
     </http>
 
     <beans:bean id="myAuthenticationProvider"
        class="net.michir.config.custom.MyAuthenticationProvider" />
 
 ```
 
2. **Custom Authentication Provider** (see _MyAuthenticationProvider_) Remeber in (1.) that, once requirements met, the Custom Filter
 fires `authenticationManager.authenticate((MyAuthentication) authentication)` the authentication manager then calls the
 registered _Authentication Providers_ one after the other until a candidate _supports_ the _(MyAuthentication) Authentication_ class type
 
 ```
     public class MyAuthenticationProvider implements AuthenticationProvider {
     
         // ...
     
         @Override
         public boolean supports(Class<?> authentication) {
             return MyAuthentication.class.isAssignableFrom(authentication);
         }
     }
 ```
 
 The _Authentication Manager_ delegates then, to the Authentication Provider, the task of loading _UserDetails_ from input 
 _(MyAuthentication) Authentication_ instance. _UserDetails_ is nothing but a User info wrapper (Spring offers a default implementation 
 through _org.springframework.security.core.userdetails.User_ class) 
 
 ```
    public class MyAuthenticationProvider implements AuthenticationProvider {
 
        @Autowired
        private UserDetailsService userDetailsService;
        
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
         if (!supports(authentication.getClass())) {
             return null;
         }
         MyAuthentication myAuthentication = (MyAuthentication) authentication;
         UserDetails userDetails = userDetailsService.loadUserByUsername(myAuthentication.getToken());
        
         return new MyAuthentication(userDetails, userDetails.getUsername(), userDetails.getAuthorities(), true);
        }
    }
 ```

3. **Custom User Details Service** (see _MyUserDetailsService_) the _Authentication Provider_ relies on a _UserDetailsService_ to load
 user information (username/login, roles, ... etc). It can be a IOC-Scanned Spring _@Component_ or any other bean. It is responsible of
 loading user information from input _token_ (extracted from the request) as specified from its single method/signature
 
 ```
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
 ```
 where _username_ stands for any String parameter/token used in the authentication process. In this example application, the token
 is a _Base64_ encoded passed in "Authorization: Token" parameter (just for fun) 

## CORS Config
I did not manage to configure freely the CORS using the Spring Security _cors_ facility, so I replaced the Spring CORS Configuration Bean with
an OPTIONS filter bean (for the preflight request), as you can find in this example.

As this is an API application, this CORS filter _permitAll()_ requests on request _OPTIONS_ method and returns, the _Origin_ request header as the
 response _Access-Control-Allow-Origin_ Header.


## Session management
Again, as this is an API application, session management (creation) has been disabled

```
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```

or in XML configuration

```
    <http ... create-session="stateless" ...>
```

## Extension to OAuth 2 protocol
You can easily start from this example application to implement the OAuth2 protocol along with the JWT token format - this latter does
not need any Database access - just replacing, in the Filter, the _Token_ header value to _Bearer_ value and changing the 
_MyUserDetailsService_ with a more appropriate _JWTUserDetailsService_ that checks the token signature using the HMAC algorithm
along with a _secret key_

(and these were actually my first motivations for this template)

# Secured endpoints and test
For test purposes, the application configures and exposes three group of API's. These API are declared using JEE 7 JAXRS
api `@Path` in _Endpoint.class_ and _PrivateEndpoint_ classes :

* _/public/**_ unsecured, publicly accessible API (generally suited for utilities accessible from within a JS Single page application 
 on unsecured domain/area)
* _/**_ secured, limited to authenticated users having a _USER_ role - corresponding to what we generally describe as Rest User Resources.
 _In OAuth2, such users generally log in using a login page (implicit flow for example)_. 
* _/private/**_ secured, limited to users having an _APPLICATION_ role. I describe as _APPLICATION_ role users such users 
 that do not correspond to physical users, but rather to systems (system 2 system calls). In OAuth2, these users
 correspond to _clients_ that have obtained there token using the _Client Credentials_ flow.
 
## Tests
The application _hard code_ checks (in _MyUserDetailsService_) for 3 user logins 

* "user:user": a user account with _USER_ role
* "app:app": a user account with _APPLICATION_ role
* "mixed:mixed": a user account with both roles (USER and APPLICATION)

Once deployed, you can test the different endpoints and the security config, executing the followings:
_(assumed the application is accessible on localhost:8080)_

**User resources API access**
```
    mytoken=`echo user:user | base64` && curl -vX GET localhost:8080/myapi/ -H "Authorization: Token $mytoken"
```
you should get a 200 code response.

```
    mytoken=`echo mixed:mixed | base64` && curl -vX GET localhost:8080/myapi/ -H "Authorization: Token $mytoken"
```
you should get a 200 code response too.

```
    mytoken=`echo app:app | base64` && curl -vX GET localhost:8080/myapi/ -H "Authorization: Token $mytoken"
```
* you should get a 403 code response.


**Application API access**
```
    curl -vX GET http://localhost:8080/myapi/  -H "Authorization: Token YXBwOmFwcAo="
```
hint: base64.encode(app:app) = "YXBwOmFwcAo="

### Context

* JEE 7 (with a deployment file _/WEB-INF/jboss-web.xml_ for wildfly/jboss Application Server, but technically the application
    can be deployed on any JEE 7 compliant Application Server)
* Spring Security 4.2.3.RELEASE
* Java 1.8+ (some lambdas, but can be easily replaced using regular Java foreach loops)




