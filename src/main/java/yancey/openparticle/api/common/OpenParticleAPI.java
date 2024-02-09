package yancey.openparticle.api.common;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.common.bridge.Bridge;
import yancey.openparticle.api.common.bridge.Logger;
import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.data.identifier.IdentifierCache;

import java.io.*;

public class OpenParticleAPI {

    public final Bridge bridge;
    public final Logger logger;

    public OpenParticleAPI(Bridge bridge, Logger logger) {
        this.bridge = bridge;
        this.logger = logger;
    }

    public long getParticleCount(DataParticleManager dataParticleManager) {
        return dataParticleManager.getParticleCount();
    }

    public void output(@NotNull File file, @NotNull DataParticleManager dataParticleManager) {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                logger.warn("文件夹创建失败 : " + parent.getAbsolutePath());
            }
        }
        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            IdentifierCache.writeToFile(dataOutputStream);
            dataParticleManager.writeToFile(dataOutputStream);
        } catch (IOException e) {
            logger.warn("文件写入失败 : " + file.getAbsolutePath(), e);
        }
    }

    public DataParticleManager input(@NotNull File file) {
        try (DataInputStream dataOutputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            IdentifierCache.readFromFile(dataOutputStream);
            return new DataParticleManager(dataOutputStream);
        } catch (IOException e) {
            logger.warn("文件读取失败 : " + file.getAbsolutePath(), e);
        }
        return null;
    }
}
