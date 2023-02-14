package io.jenkins.plugins.obs.utils;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import io.jenkins.cli.shaded.jakarta.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * <h1>공통 유틸</h1>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtil {

    /**
     * 젠킨스 환경 변수를 주입합니다.
     *
     * @param original 원본
     * @param env      환경 변수
     * @return 환경 변수가 주입된 값
     */
    public static String injectEnv(String original, EnvVars env) {
        return Util.replaceMacro(original, env);
    }

    /**
     * 대상 폴더에 젠킨스 환경 변수를 주입합니다.
     *
     * @param original 원본
     * @param env      환경 변수
     * @return 환경 변수가 주입된 값
     */
    public static String injectEnvToTargetFolder(String original, EnvVars env) {
        String result = CommonUtil.injectEnv(original, env);
        if (!StringUtils.isEmpty(result) && !result.endsWith("/")) {
            result = result + "/";
        }
        return result;
    }

    /**
     * 업로드 파일명을 생성합니다.
     *
     * @param workspace 젠킨스 작업공간
     * @param file       업로드할 파일
     * @return 젠킨스 작업 공간 이하의 경로가 포함된 파일명
     */
    public static String generateUploadFileName(FilePath workspace, FilePath file) {
        String result = file.getName();
        try {
            final String workspaceURIPath = workspace.toURI().getPath();
            return file.toURI().getPath().replaceFirst(workspaceURIPath, "");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 파일의 Content Type 을 반환합니다.
     *
     * @param fileName 파일명
     * @return 파일의 Content Type
     */
    public static String getContentType(String fileName) {
        Path path = Paths.get(fileName);
        String result = null;
        try {
            result = Files.probeContentType(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null) {
            result = new MimetypesFileTypeMap().getContentType(fileName);
        }
        return result;
    }
}
