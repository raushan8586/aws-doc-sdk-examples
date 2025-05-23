// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import com.example.cloudformation.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.regions.Region;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * To run these integration tests, you must set the required values
 * in the config.properties file or AWS Secrets Manager.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CloudFormationTest {
    private static final Logger logger = LoggerFactory.getLogger(CloudFormationTest.class);
    private static CloudFormationClient cfClient;
    private static String stackName = "";
    private static String roleARN = "";
    private static String location = "";
    private static String key = "";
    private static String value = "";

    @BeforeAll
    public static void setUp() {
        cfClient = CloudFormationClient.builder()
                .region(Region.US_EAST_1)
                .build();

        // Get the values to run these tests from AWS Secrets Manager.
        Gson gson = new Gson();
        String json = getSecretValues();
        SecretValues values = gson.fromJson(json, SecretValues.class);
        stackName = values.getStackName();
        roleARN = values.getRoleARN();
        location = values.getLocation();
        key = values.getKey();
        value = values.getValue();
    }

    @Test
    @Tag("IntegrationTest")
    @Order(1)
    public void testCreateStack() {
        assertDoesNotThrow(() -> CreateStack.createCFStack(cfClient, stackName, roleARN, location));
        logger.info("Test 1 passed");
    }

    @Test
    @Tag("IntegrationTest")
    @Order(2)
    public void testDescribeStacks() {
        assertDoesNotThrow(() -> DescribeStacks.describeAllStacks(cfClient));
        logger.info("Test 2 passed");
    }

    @Test
    @Tag("IntegrationTest")
    @Order(3)
    public void testGetTemplate() {
        assertDoesNotThrow(() -> GetTemplate.getSpecificTemplate(cfClient, stackName));
        logger.info("Test 3 passed");
    }

    @Test
    @Tag("IntegrationTest")
    @Order(4)
    public void testDeleteStack() {
        assertDoesNotThrow(() -> DeleteStack.deleteSpecificTemplate(cfClient, stackName));
        logger.info("Test 4 passed");
    }

    private static String getSecretValues() {
        SecretsManagerClient secretClient = SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .build();
        String secretName = "test/cloudformation";

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretClient.getSecretValue(valueRequest);
        return valueResponse.secretString();
    }

    @Nested
    @DisplayName("A class used to get test values from test/cloudformation (an AWS Secrets Manager secret)")
    class SecretValues {
        private String stackName;
        private String roleARN;
        private String location;

        private String key;

        private String value;

        public String getStackName() {
            return stackName;
        }

        public String getRoleARN() {
            return roleARN;
        }

        public String getLocation() {
            return location;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

}
