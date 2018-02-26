#Online Translation
Leverage Baidu online translation API, to build an simple translation plugin of browser.

Application doesn't persist any data. The embedded H2 database is for cache purpose only. It's dropped and recreated during application startup.

baidu.key file contains the appid and securityKey to use Baidu Fanyi service. Make sure it's put in the classpath. 

CAUTION:
baidu.key SHOULD NOT SHARED, DON'T UPLOAD IT TO GIT.


###Plan:
>       1.不考虑安全验证，实现翻译接口。
>       2.实现浏览器插件，并和后台服务集成。
>       3.加入安全验证。
>       4.加入缓存，优化性能。

###Baidu Fanyi
http://api.fanyi.baidu.com/api/trans/product/index


###Swagger flow:

localhost:7070
    -> redirect to 

###Notes:  
1. dir contains swagger-ui static pages should be consistent in these 3 places: 
>       content folder "src/main/resource/api-spec"  
>       SwaggerConfig.java - addResourceLocations("classpath:/api-spec/")  
>       TranslationRestEndpoint.java - response.sendRedirect("api-spec/index.html")  

2. URL to generate/consume the api meta file should be consistent in these 2 places:
>       $project.home/src/main/resource/api-spec/index.html - swaggerSpecPath = '/swagger-spec.json'
>       application.yml - springfox:documentation:swagger:v2:path: /swagger-spec.json (check the app port as well)

To get the prod war (excluded swagger):
gradle clean prodExclude war -Dspring.profiles.active=$pringBootProfileName


