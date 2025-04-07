//package idv.clu.gateway.iam;
//
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.inject.Produces;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//
///**
// * @author clu
// */
//@ApplicationScoped
//public class KeycloakAdminClientProducer {
//
//    @Produces
//    @ApplicationScoped
//    public Keycloak produceKeycloak() {
//        return KeycloakBuilder.builder()
//                .serverUrl("http://localhost:8080/auth")
//                .realm("")
//                .clientId("")
//                .grantType("")
//                .username("")
//                .password("")
//                .build();
//    }
//
//}
