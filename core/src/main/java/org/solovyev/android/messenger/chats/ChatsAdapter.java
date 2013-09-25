package org.solovyev.android.messenger.chats;

import android.content.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserEventType;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:48 PM
 */
public class ChatsAdapter extends MessengerListItemAdapter<ChatListItem> /*implements ChatEventListener, UserEventListener*/ {

	public ChatsAdapter(@Nonnull Context context) {
		super(context, new ArrayList<ChatListItem>(), false);
	}

	/*@Override*/
	public void onEvent(@Nonnull UserEvent event) {
		final User eventUser = event.getUser();
		switch (event.getType()) {
			case chat_removed:
				final String chatId = event.getDataAsChatId();
				removeListItem(eventUser, chatId);
				break;
			case chat_added:
				final Chat chat = event.getDataAsChat();
				addListItem(eventUser, chat);
				break;
			case chat_added_batch:
				final List<Chat> chats = event.getDataAsChats();
				addAll(Lists.transform(chats, new Function<Chat, ChatListItem>() {
					@Override
					public ChatListItem apply(@javax.annotation.Nullable Chat chat) {
						assert chat != null;
						return ChatListItem.newInstance(eventUser, chat);
					}
				}));
				break;
		}
	}

	protected void removeListItem(@Nonnull User user, @Nonnull String chatId) {
		removeListItem(user, ChatImpl.newFakeChat(chatId));
	}

	protected void removeListItem(@Nonnull User user, @Nonnull Chat chat) {
		remove(ChatListItem.newEmpty(user, chat));
	}

	protected void addListItem(@Nonnull User user, @Nonnull Chat chat) {
		addListItem(ChatListItem.newInstance(user, chat));
	}

	@Override
	protected Comparator<? super ChatListItem> getComparator() {
		return ChatListItemComparator.getInstance();
	}

	@Nonnull
	private ChatService getChatService() {
		return App.getChatService();
	}

	/*@Override*/
	public void onEvent(@Nonnull ChatEvent event) {
		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case changed:
			case last_message_changed:
			case unread_message_count_changed:
				try {
					final User user = App.getAccountService().getAccountById(eventChat.getEntity().getAccountId()).getUser();
					final ChatListItem chatListItem = findInAllElements(user, eventChat);
					if (chatListItem != null) {
						chatListItem.onEvent(event);
						notifyDataSetChanged();
					}
				} catch (UnsupportedAccountException e) {
					App.getExceptionHandler().handleException(e);
				}
				break;
		}
	}

	@Nullable
	protected ChatListItem findInAllElements(@Nonnull User user, @Nonnull Chat chat) {
		return Iterables.find(getAllElements(), Predicates.<ChatListItem>equalTo(ChatListItem.newEmpty(user, chat)), null);
	}
}
