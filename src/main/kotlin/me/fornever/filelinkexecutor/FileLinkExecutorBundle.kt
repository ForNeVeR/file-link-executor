/*
 * SPDX-FileCopyrightText: 2022 Friedrich von Never <friedrich@fornever.me>
 * SPDX-FileCopyrightText: 2026 file-link-executor contributors <https://github.com/ForNeVeR/file-link-executor>
 *
 * SPDX-License-Identifier: MIT
 */

package me.fornever.filelinkexecutor

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val BUNDLE = "messages.FileLinkExecutorBundle"

object FileLinkExecutorBundle {
    private val instance = DynamicBundle(FileLinkExecutorBundle::class.java, BUNDLE)

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        instance.getMessage(key, *params)

    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> =
        instance.getLazyMessage(key, *params)
}
