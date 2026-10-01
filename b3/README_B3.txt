1. Copy src/ into the project (merge). No changes to B2 files.
2. Test deps: spring-boot-starter-test (JUnit 5, Mockito, AssertJ, junit-jupiter-params) - normally already present.
3. GET /blood-groups* requires authentication (B2 default). If the donor registration screen must load the group
   list before login, add to SecurityConfig: .requestMatchers(HttpMethod.GET, "/blood-groups").permitAll()
4. Assumed columns: blood_group(id, code), blood_compatibility(recipient_group_id, donor_group_id).
5. Run: mvn test -Dtest=BloodGroupServiceTest
