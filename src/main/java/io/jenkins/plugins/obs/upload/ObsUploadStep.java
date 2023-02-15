package io.jenkins.plugins.obs.upload;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.obs.constants.LoggerConstants;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * <h1>NHN Cloud Object Storage 업로드 스탭</h1>
 */
@Getter
public class ObsUploadStep extends Builder implements SimpleBuildStep {

    /**
     * <h3>API HOST</h3>
     */
    private String host;
    /**
     * <h3>Jenkins Credential</h3>
     * AWS S3 자격증명 Secret Key 와 Access Key 를 각각 username 과 password 로 가지는 Credential 이 필요합니다.
     */
    private String credentialsId;
    /**
     * <h3>Object Storage 컨테이너명</h3>
     */
    private String containerName;
    /**
     * <h3>타겟 디렉토리</h3>
     * 파일이 업로드될 Object Storage 컨테이너의 디렉토리.
     * 해당 디렉토리 하위에 파일이 업로드됩니다.
     */
    private String targetFolder;
    /**
     * <h3>업로드 포함 대상</h3>
     * GLOB pattern 으로 업로드 대상을 지정합니다.
     */
    private String includes;
    /**
     * <h3>업로드 제외 대상</h3>
     * GLOB pattern 으로 업로드 제외 대상을 지정합니다.
     */
    private String excludes;


    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher, @NonNull TaskListener listener)
        throws InterruptedException, IOException {
        try {
            // 업로드
            new ObsUploader(run, workspace, env, launcher, listener, this).start();
        } catch (Exception e) {
            // 실패 상태 처리
            run.setResult(Result.FAILURE);
            // 오류 로깅
            listener.getLogger().println(String.format("%s %s 업로드 중 오류가 발생했습니다. :: %s", LoggerConstants.PREFIX, LoggerConstants.UPLOAD_STEP, e.getMessage()));
            e.printStackTrace();
            // 예외 던지기
            throw new AbortException(String.format("%s %s 업로드 실패", LoggerConstants.PREFIX, LoggerConstants.UPLOAD_STEP));
        }
    }

    @DataBoundConstructor
    public ObsUploadStep(String host, String credentialsId, String containerName, String targetFolder, String includes, String excludes) {
        this.host = host;
        this.credentialsId = credentialsId;
        this.containerName = containerName;
        this.targetFolder = targetFolder;
        this.includes = includes;
        this.excludes = excludes;
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @DataBoundSetter
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    @DataBoundSetter
    public void setIncludes(String includes) {
        this.includes = includes;
    }

    @DataBoundSetter
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * <h2>플러그인 Step Descriptor</h2>
     * 젠킨스에 플러그인에 대한 정보를 제공하고 인스턴스화 하는데 필요한 클래스
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "NHN Cloud Object Storage 업로드";
        }

        @Override
        public boolean isApplicable(Class jobType) {
            return true;
        }

        /**
         * Credentials Select box 생성
         */
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            return new StandardListBoxModel()
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class)
                .includeCurrentValue(credentialsId);
        }
    }
}
