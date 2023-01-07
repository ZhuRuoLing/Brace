package dev.dubhe.brace.plugins;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.dubhe.brace.BraceServer;
import dev.dubhe.brace.utils.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManager {
    private static final Logger LOGGER = BraceServer.getBraceBot().getLogger();
    protected final Map<String, PluginInstance> pluginFiles = new ConcurrentHashMap<>();
    private final Path PLUGINS_PATH;

    public PluginManager() {
        PLUGINS_PATH = BraceServer.ROOT.resolve("plugins");
    }

    public PluginManager(Path PLUGINS_PATH) {
        this.PLUGINS_PATH = PLUGINS_PATH;
    }

    public void init() {
        LOGGER.info(new TranslatableComponent("brace.plugin.plugin_manager.load").getString());
        LOGGER.info(new TranslatableComponent("brace.plugin.plugins.loading").getString());
        scanForPlugin(true);
        initAllPlugin();
    }

    @Nullable
    @CanIgnoreReturnValue
    public File[] scanForPlugin(boolean registerPlugin) {
        File plugins = PLUGINS_PATH.toFile();
        if (!plugins.isDirectory() || !plugins.exists()) {
            if (!plugins.mkdirs()) return null;
        }
        File[] files = plugins.listFiles(((dir, name) -> name.endsWith(".jar") && dir.toPath().resolve(name).toFile().isFile()));
        if (files != null && registerPlugin) {
            for (File file : files) {
                try {
                    registerPlugin(file);
                } catch (IOException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    LOGGER.warn(new TranslatableComponent("brace.plugin.load.warn.dont_has_main_class", file.getName()).getString());
                }
            }
        }
        return files;
    }

    public void initPlugin(String pluginId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (this.pluginFiles.containsKey(pluginId)){
            this.pluginFiles.get(pluginId).init();
        }
        throw new IllegalArgumentException("Illegal plugin id %s (Not Exist)".formatted(pluginId));
    }

    public void initAllPlugin() throws RuntimeException{
        this.pluginFiles.forEach((s, pluginInstance) -> {
            try {
                initPlugin(s);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 注册插件到此{@link PluginManager}中
     *
     * @param file 插件文件
     * @throws IOException               读取插件文件出错
     * @throws ClassNotFoundException    找不到插件入口类
     * @throws InvocationTargetException 插件初始化过程中抛出异常
     * @throws NoSuchMethodException     找不到插件入口方法
     * @throws IllegalAccessException    没有插件方法访问权限
     * @throws InstantiationException    无法对插件主类实例化
     */
    public void registerPlugin(@NotNull File file) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        PluginInstance pluginFile = new PluginInstance(file);
        String pluginID = pluginFile.getMetaData().id;
        if (!this.pluginFiles.containsKey(pluginID)) {
            this.pluginFiles.put(pluginID, pluginFile);
        } else {
            LOGGER.warn(new TranslatableComponent("brace.plugin.load.warn.has_same_id_plugin", file.getName()).getString());
        }
    }

    /**
     * 如果没有指定插件id，对所有插件进行init操作
     * 如果指定插件id，对指定插件进行init
     * 若插件id不存在，抛出IllegalArgumentException
     */
    public void onInitialization() {
        pluginFiles.forEach((s, pluginFile) -> {
            onInitialization(s);
        });
    }

    /**
     * 如果没有指定插件id，对所有插件进行init操作
     * 如果指定插件id，对指定插件进行init
     * 若插件id不存在，抛出IllegalArgumentException
     *
     * @param pluginId 插件id
     * @throws IllegalArgumentException 插件id所对应的插件文件不存在
     */
    public void onInitialization(@NotNull String pluginId) {
        if (pluginFiles.containsKey(pluginId)) {
            pluginFiles.get(pluginId).onInitialization();
        } else {
            throw new IllegalArgumentException("Plugin id %s not found".formatted(pluginId));
        }
    }

    /**
     * 如果没有指定插件id，对所有插件进行uninstall操作
     * 如果指定插件id，对指定插件进行uninstall
     * 若插件id不存在，抛出IllegalArgumentException
     */
    public void onUninstall() {
        pluginFiles.forEach((s, pluginFile) -> {
            onUninstall(s);
        });
    }


    /**
     * 如果没有指定插件id，对所有插件进行uninstall操作
     * 如果指定插件id，对指定插件进行uninstall
     * 若插件id不存在，抛出IllegalArgumentException
     * @param pluginId 插件id
     * @throws IllegalArgumentException 插件id所对应的插件文件不存在
     */
    public void onUninstall(@NotNull String pluginId) {
        if (pluginFiles.containsKey(pluginId)) {
            pluginFiles.get(pluginId).onUninstall();
        } else {
            throw new IllegalArgumentException("Plugin id %s not found".formatted(pluginId));
        }
    }


}
