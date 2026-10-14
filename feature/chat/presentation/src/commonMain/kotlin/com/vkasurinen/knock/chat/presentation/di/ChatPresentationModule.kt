package com.vkasurinen.knock.chat.presentation.di

import com.vkasurinen.knock.chat.presentation.chat_detail.ChatDetailViewModel
import com.vkasurinen.knock.chat.presentation.chat_list.ChatListViewModel
import com.vkasurinen.knock.chat.presentation.chat_list_detail.ChatListDetailViewModel
import com.vkasurinen.knock.chat.presentation.create_chat.CreateChatViewModel
import com.vkasurinen.knock.chat.presentation.manage_chat.ManageChatViewModel
import com.vkasurinen.knock.chat.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatListDetailViewModel)
    viewModelOf(::CreateChatViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::ManageChatViewModel)
    viewModelOf(::ProfileViewModel)
}