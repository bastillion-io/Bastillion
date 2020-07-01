![Bastillion](https://www.bastillion.io/images/bastillion_40x40.png)
Bastillion
======
Bastillion es una consola SSH basada en web que gestiona centralmente el acceso administrativo a los sistemas. La administración basada en web se combina con la administración y distribución de claves SSH públicas del usuario. La gestión y administración de claves se basa en perfiles asignados a usuarios definidos.

Los administradores pueden iniciar sesión utilizando la autenticación de dos factores con [Authy] (https://authy.com/) o [Google Authenticator] (https://github.com/google/google-authenticator). Desde allí, pueden administrar sus claves SSH públicas o conectarse a sus sistemas a través de un shell web. Los comandos se pueden compartir entre shells para facilitar la aplicación de parches y eliminar la ejecución redundante de comandos.

Bastillion coloca TLS / SSL sobre SSH y actúa como un host de bastión para la administración. Los protocolos están apilados (TLS / SSL + SSH), por lo que la infraestructura no puede exponerse a través del túnel / reenvío de puertos. Puede encontrar más detalles en el siguiente documento técnico: Implementación de un sistema de terceros de confianza para Secure Shell. Además, la administración de claves SSH está habilitada de manera predeterminada para evitar claves públicas no administradas y aplicar las mejores prácticas.

![Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

Bastillion Releases
------
Bastillion está disponible para uso gratuito bajo la Licencia pública general de Affero

https://github.com/bastillion-io/Bastillion/releases

Prerrequisitos
-------------
**Open-JDK / Oracle-JDK - 1.9 o mayor**

**Instalar [Authy](https://authy.com/) o [Google Authenticator](https://github.com/google/google-authenticator)** para habilitar la autenticación de dos factores con Android o iOS

| Aplicación          | Android                                                                                             | iOS                                                                        |             
|----------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| Authy                | [Google Play](https://play.google.com/store/apps/details?id=com.authy.authy)                        | [iTunes](https://itunes.apple.com/us/app/authy/id494168017)                |
| Google Authenticator | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

Para correr incluido con Jetty
------
Descarga bastillion-jetty-vXX.XX.tar.gz

https://github.com/bastillion-io/Bastillion/releases

Exportar variables de entorno

para Linux/Unix/OSX

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

Inicia Bastillion

para Linux/Unix/OSX

        ./startBastillion.sh
	
Más documentación en: https://www.bastillion.io/docs/index.html
	
Utilizando Bastillion
------
Abra el navegador en la URL https://\ip\>:8443

Inicio de sesion

	Usuario:admin
	Contraseña:changeme
	

Administrar claves SSH
------
Por defecto, Bastillion sobrescribirá todos los valores en el archivo autorizado de claves autorizadas para un sistema. Puede deshabilitar la administración de claves editando el archivo BastillionConfig.properties y usar Bastillion solo como host de bastión. Este archivo se encuentra en el directorio jetty / bastillion / WEB-INF / classes. (o el directorio src / main / resources si se construye desde la fuente)

	#set to false para deshabilitar la administración de claves. Si es falso, la clave pública Bastillion se agregará a la authorized_keys archivo (en lugar de que se sobrescriba por completo).
	keyManagementEnabled=false

Además, el archivo Authorized_keys se actualiza / actualiza periódicamente en función de las relaciones definidas en la aplicación. Si la administración de claves está habilitada, el intervalo de actualización se puede especificar en el archivo BastillionConfig.properties.itten completamente).

	#authorized_keys intervalo de actualización en minutos (sin actualización para <= 0)
	authKeysRefreshInterval=120

De forma predeterminada, Bastillion generará y distribuirá las claves SSH administradas por los administradores mientras les hace descargar el privado generado. Esto obliga a los administradores a utilizar frases de contraseña seguras para las claves que se establecen en los sistemas. La clave privada solo está disponible para descargar una vez y no se almacena en el lado de la aplicación. Para deshabilitar y permitir que los administradores establezcan cualquier clave pública, edite BastillionConfig.properties.

	#set to true para generar claves cuando los usuarios las agregan / administran y aplican frases de contraseña fuertes establecidas en false para permitir a los usuarios establecer su propia clave pública
	forceUserKeyGeneration=false

Suministro de un par de claves SSH personalizado
------
Bastillion genera su propia clave SSH pública/privada en el inicio inicial para su uso al registrar sistemas. Puede especificar un par de claves SSH personalizado en el archivo BastillionConfig.properties.

Por ejemplo:

	#establecido en verdadero para regenerar e importar claves SSH  --set to true
	resetApplicationSSHKey=true

	#Tipo de clave SSH 'dsa' o 'rsa'
	sshKeyType=rsa

	#private key  --set pvt key
	privateKey=/Users/kavanagh/.ssh/id_rsa

	#public key  --set pub key
	publicKey=/Users/kavanagh/.ssh/id_rsa.pub
	
	#contraseña predeterminada  --dejar en blanco si la frase de contraseña está vacía
	defaultSSHPassphrase=myPa$$w0rd
	
Después del inicio y una vez que la clave ha sido registrada, se puede eliminar del sistema. La frase de contraseña y las rutas de acceso clave se eliminarán del archivo de configuración.

Revisión de cuentas
------
La auditoría está deshabilitada de forma predeterminada. Los registros de auditoría se pueden habilitar a través de **log4j2.xml** Al descomentar la **io.bastillion.manage.util.SystemAudit** y el **audit-appender** definicion.

> https://github.com/bastillion-io/Bastillion/blob/master/src/main/resources/log4j2.xml#L19-L22
	
La auditoría a través de la aplicación es solo una prueba de concepto. Se puede habilitar en BastillionConfig.properties.

	#enable audit  --set to true to enable
	enableInternalAudit=true

Screenshots
-----------
![Login](https://www.bastillion.io/images/screenshots/medium/login.png)

![Two-Factor](https://www.bastillion.io/images/screenshots/medium/two-factor.png)

![More Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

![Manage Systems](https://www.bastillion.io/images/screenshots/medium/manage_systems.png)

![Manage Users](https://www.bastillion.io/images/screenshots/medium/manage_users.png)

![Define SSH Keys](https://www.bastillion.io/images/screenshots/medium/manage_keys.png)

![Disable SSH Keys](https://www.bastillion.io/images/screenshots/medium/disable_keys.png)

AGPL License
-----------
Bastillion está disponible para su uso bajo la Licencia pública general de Affero
