package idv.clu.gateway.common.enums;

/**
 * @author clu
 */
public enum Environment {

    DEV,
    CONTAINER,
    PRODUCTION;

    public static boolean isTestEnvironment(String environment) {
        return switch (Environment.valueOf(environment.toUpperCase())) {
            case DEV, CONTAINER -> true;
            default -> false;
        };
    }

    public static boolean isContainerEnvironment(String environment) {
        return Environment.valueOf(environment.toUpperCase()).equals(CONTAINER);
    }

}
