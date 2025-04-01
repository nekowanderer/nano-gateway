FROM quay.io/keycloak/keycloak:26.1.4 as builder

ARG KEYCLOAK_CONF_FILE=conf/local/keycloak.conf

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak

COPY ${KEYCLOAK_CONF_FILE} /opt/keycloak/conf/
COPY keystore/nano_keycloak_key.keystore /opt/keycloak/conf/

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.1.4

COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--optimized"]
