package io.jenkins.plugins.obs.utils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Run;
import io.minio.MinioClient;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.security.auth.login.CredentialNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientUtil {

    public static final MinioClient getClient(String host, String credentialsId, @Nonnull Run<?, ?> run) throws CredentialNotFoundException {
        StandardUsernamePasswordCredentials credentials = Optional.ofNullable(
            CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, run)
        ).orElseThrow(CredentialNotFoundException::new);

        return MinioClient.builder()
                .endpoint(host)
                .credentials(credentials.getUsername(), credentials.getPassword().getPlainText())
                .build();
    }

}
