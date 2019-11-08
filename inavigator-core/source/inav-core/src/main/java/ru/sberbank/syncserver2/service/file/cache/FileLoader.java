package ru.sberbank.syncserver2.service.file.cache;

import ru.sberbank.syncserver2.service.log.TagLogger;

public interface FileLoader {
    byte[] loadSingleChunk(String folder, int chunkIndex);
    TagLogger getTagLogger();
}
