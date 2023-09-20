package org.pentaho.di.steps.sns;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.UUID;

public class SnsTest {
  public static final String AWS_ACCESS_KEY = "AKIAYCTF77XMUTIALJA6";
  public static final String AWS_SECRET_KEY = "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";

  public static void main( String[] args ) {
    publishMessage();
  }

  private static void publishMessage() {
    SnsClient snsClient = getSnsClient();
    try {
      String topicArn = "arn:aws:sns:ap-south-1:555338431961:pentaho-sns-plugin-topic";
      String subject = "Test Subject";
      String message = "This is test message" + UUID.randomUUID();

      PublishRequest request = PublishRequest.builder()
        .message(message)
        .topicArn(topicArn)
        .build();

      PublishResponse result = snsClient.publish(request);
      System.out.println(result);
    } catch (Exception e) {
      System.out.println("Failed to send SNS notification: " + e.getMessage());
    } finally {
      snsClient.close();
    }
  }

  private static SnsClient getSnsClient() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create( AWS_ACCESS_KEY, AWS_SECRET_KEY );
    return SnsClient.builder()
      .credentialsProvider(()-> credentials )
      .region( Region.AP_SOUTH_1)
      .build();
  }
}
