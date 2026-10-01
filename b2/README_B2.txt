1. Copy src/ into the project (merge).
2. DELETE config/BootstrapSecurityConfig.java.
3. pom.xml: add io.jsonwebtoken jjwt-api, jjwt-impl (runtime), jjwt-jackson (runtime) all 0.12.x,
   plus spring-boot-starter-security and spring-boot-starter-validation if missing.
4. application.yml: app.jwt.secret: ${JWT_SECRET}  and  app.jwt.expiration-minutes: 60
5. OpenApiConfig should define a "bearerAuth" security scheme (used by @SecurityRequirement).
6. Check README assumptions (column names, BusinessException constructor, ApiError keys) against B0/B1.
