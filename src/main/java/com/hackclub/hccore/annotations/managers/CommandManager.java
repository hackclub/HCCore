package com.hackclub.hccore.annotations.managers;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.annotations.AnnotationParser;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.logging.Logger;

public class CommandManager {

  private final static String mainPkgName = "com.hackclub.hccore";
  public static void registerCommands(HCCorePlugin instance, BukkitScheduler scheduler, AnnotationParser<CommandSender> annotationParser) {
    String[] packages = {
        "commands",
        "commands.messaging"
    };

    for (String pkg : packages) {
      registerCommandsFromPackages(mainPkgName + "." + pkg, instance, scheduler, annotationParser);
    }
  }

  private static void registerCommandsFromPackages(String pkg, HCCorePlugin instance, BukkitScheduler scheduler, AnnotationParser<CommandSender> annotationParser) {
    Logger logger = HCCorePlugin.getInstance().getLogger();

    Reflections reflections = new Reflections(pkg, Scanners.TypesAnnotated);

    Set<Class<?>> commands = reflections.getTypesAnnotatedWith(RegisteredCommand.class);

    long start = System.currentTimeMillis();
    int success = 0;
    int failed = 0;
    for (Class<?> commandClass : commands) {
      try {
        Object command = createInstance(commandClass, instance, scheduler);
        RegisteredCommand annotation = commandClass.getAnnotation(RegisteredCommand.class);

        annotationParser.parse(command);
        success++;
      } catch (Exception e) {
        logger.severe(e.getMessage());
        failed++;
      }
    }
    long end = System.currentTimeMillis() - start;
    logger.info("Registered " + commands.size() + " commands from " + pkg + " in " + end + " ms");
    logger.info("Loaded commands: " + success);
    logger.info("Failed to load: " + failed);
  }

  private static Object createInstance(Class<?> clazz, HCCorePlugin instance, BukkitScheduler scheduler) throws Exception {
    try {
      return clazz.getConstructor(
          HCCorePlugin.class,
          BukkitScheduler.class
      ).newInstance(instance, scheduler);
    } catch (NoSuchMethodException e) {
      try {
        return clazz.getConstructor(
            HCCorePlugin.class
        ).newInstance(instance);
      } catch (NoSuchMethodException ex) {
        return clazz.getConstructor().newInstance();
      }
    }
  }
}
