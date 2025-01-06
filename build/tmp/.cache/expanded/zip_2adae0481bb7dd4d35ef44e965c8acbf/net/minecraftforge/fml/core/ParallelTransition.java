/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.core;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.IModStateTransition;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ThreadSelector;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import net.minecraftforge.fml.IModStateTransition.EventGenerator;

record  ParallelTransition(ModLoadingStage stage, BiFunction<ModContainer, ModLoadingStage, ParallelDispatchEvent> event) implements IModStateTransition {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event & IModBusEvent> EventGenerator<T> eventFunction() {
        return EventGenerator.fromFunction(mod -> (T)event.apply(mod, stage));
    }

    @Override
    public ThreadSelector threadSelector() {
        return ThreadSelector.PARALLEL;
    }

    @Override
    public BiFunction<Executor, CompletableFuture<Void>, CompletableFuture<Void>> finalActivityGenerator() {
        return (e, prev) -> prev.thenApplyAsync(t -> {
            stage.getDeferredWorkQueue().runTasks();
            return t;
        }, e);
    }
}
