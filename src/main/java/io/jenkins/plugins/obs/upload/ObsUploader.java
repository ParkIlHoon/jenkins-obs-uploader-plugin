package io.jenkins.plugins.obs.upload;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.obs.constants.LoggerConstants;
import io.jenkins.plugins.obs.utils.ClientUtil;
import io.jenkins.plugins.obs.utils.CommonUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.login.CredentialNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * <h1>NHN Cloud Object Storage 업로더</h1>
 */
@RequiredArgsConstructor
public class ObsUploader {

    private final Run<?, ?> run;
    private final FilePath workspace;
    private final EnvVars env;

    private final Launcher launcher;

    private final TaskListener listener;

    private final ObsUploadStep step;

    /**
     * 업로드
     * @throws Exception
     */
    public void start() throws CredentialNotFoundException, IOException, InterruptedException {
        // Minio Client 생성
        MinioClient client = ClientUtil.getClient(step.getHost(), step.getCredentialsId(), run);

        // Jenkins Environment Variable 적용
        final String includes = CommonUtil.injectEnv(this.step.getIncludes(), env);
        final String excludes = CommonUtil.injectEnv(this.step.getExcludes(), env);
        final String targetFolderExpanded = CommonUtil.injectEnvToTargetFolder(this.step.getTargetFolder(), env);

        // 업로드 시작
        Arrays
            .asList(workspace.list(includes, excludes))
            .forEach(filePath -> {
                String fileName = CommonUtil.generateUploadFileName(workspace, filePath);
                String contentType = CommonUtil.getContentType(fileName);

                try {
                    PutObjectArgs put = PutObjectArgs.builder()
                        .bucket(this.step.getContainerName())
                        .object(targetFolderExpanded + fileName)
                        .stream(filePath.read(), filePath.toVirtualFile().length(), -1)
                        .contentType(contentType)
                        .build();

                    client.putObject(put);

                    listener.getLogger().println(String.format("%s %s [%s] --> [%s] , mime [%s] ", LoggerConstants.PREFIX, LoggerConstants.UPLOAD_STEP, fileName, step.getContainerName(), contentType));
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    run.setResult(Result.UNSTABLE);
                }
            });
    }

}
