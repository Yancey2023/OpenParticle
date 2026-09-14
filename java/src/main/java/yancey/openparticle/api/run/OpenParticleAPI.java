package yancey.openparticle.api.run;

import yancey.openparticle.api.common.nativecore.OpenParticleProject;
import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.logger.Logger;
import yancey.openparticle.api.util.MyLogger;

import java.io.*;

public class OpenParticleAPI {

    public final OpenParticleProject.Bridge bridge;
    public final Logger logger;

    public OpenParticleAPI() {
        this.bridge = (namespace, value) -> new float[]{0, 0, 0, 0};
        this.logger = new MyLogger();
    }

    public void output(File file, DataParticleManager dataParticleManager) {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                logger.warn("文件夹创建失败 : " + parent.getAbsolutePath());
            }
        }
        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            dataParticleManager.writeToFile(dataOutputStream);
        } catch (IOException e) {
            logger.warn("文件写入失败 : " + file.getAbsolutePath(), e);
        }
    }

    public DataParticleManager input(File file) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return new DataParticleManager(dataInputStream);
        }
    }

}
