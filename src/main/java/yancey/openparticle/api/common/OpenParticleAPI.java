package yancey.openparticle.api.common;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.common.bridge.Bridge;
import yancey.openparticle.api.common.bridge.Logger;
import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.data.identifier.IdentifierCache;

import java.io.*;

public record OpenParticleAPI(Bridge bridge, Logger logger) {

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

    public DataParticleManager input(@NotNull File file) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return new DataParticleManager(dataInputStream);
        } catch (IOException e) {
            throw e;
        }
    }
}
