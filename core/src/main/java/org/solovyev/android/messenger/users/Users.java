/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.Accounts;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.view.ViewAwareTag;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.users.ContactUiEventType.call;
import static org.solovyev.android.properties.Properties.newProperty;

public final class Users {

	@Nonnull
	public static final String CONTACTS_FRAGMENT_TAG = "contacts";

	@Nonnull
	public static final String CREATE_USER_FRAGMENT_TAG = "create_user";

	@Nonnull
	private static final String ARG_USER_ID = "user_id";

	@Nonnull
	private static final String ARG_EDIT_CLASS_NAME = "edit_class_name";

	@Nonnull
	static final ContactsDisplayMode DEFAULT_CONTACTS_MODE = ContactsDisplayMode.all_contacts;

	private Users() {
	}

	@Nonnull
	public static String getDisplayNameFor(@Nonnull Entity user) {
		return App.getUserService().getUserById(user).getDisplayName();
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull String accountId,
									  @Nonnull String accountUserId,
									  @Nonnull List<AProperty> properties) {
		final Entity entity = newEntity(accountId, accountUserId);
		return newUser(entity, properties);
	}

	@Nonnull
	public static MutableUser newEmptyUser(@Nonnull Entity accountUser) {
		return newUser(accountUser, Collections.<AProperty>emptyList());
	}

	@Nonnull
	public static User newEmptyUser(@Nonnull String userId) {
		return newEmptyUser(newEntityFromEntityId(userId));
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull Entity entity,
									  @Nonnull Collection<AProperty> properties) {
		return UserImpl.newInstance(entity, properties);
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull Entity entity,
									  @Nonnull AProperties properties) {
		return UserImpl.newInstance(entity, properties.getPropertiesCollection());
	}

	public static void tryParseNameProperties(@Nonnull List<AProperty> properties, @Nullable String fullName) {
		if (fullName != null) {
			int firstSpaceSymbolIndex = fullName.indexOf(' ');
			int lastSpaceSymbolIndex = fullName.lastIndexOf(' ');
			if (firstSpaceSymbolIndex != -1 && firstSpaceSymbolIndex == lastSpaceSymbolIndex) {
				// only one space in the string
				// Proof:
				// 1. if no spaces => both return -1
				// 2. if more than one spaces => both return different
				final String firstName = fullName.substring(0, firstSpaceSymbolIndex);
				final String lastName = fullName.substring(firstSpaceSymbolIndex + 1);
				properties.add(newProperty(User.PROPERTY_FIRST_NAME, firstName));
				properties.add(newProperty(User.PROPERTY_LAST_NAME, lastName));
			} else {
				// just store full name in first name field
				properties.add(newProperty(User.PROPERTY_FIRST_NAME, fullName));
			}
		}
	}

	@Nonnull
	public static AProperty newOnlineProperty(boolean online) {
		return newProperty(User.PROPERTY_ONLINE, String.valueOf(online));
	}

	public static void fillContactPresenceViews(@Nonnull final Context context,
												@Nonnull ViewAwareTag viewTag,
												@Nonnull final User contact,
												@Nullable Account account,
												boolean showCall) {
		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		final View contactCall = viewTag.getViewById(R.id.mpp_li_contact_call_view);
		final View contactDivider = viewTag.getViewById(R.id.mpp_li_contact_divider_view);

		final boolean canCall = account != null && account.canCall(contact);
		if (showCall && canCall) {
			contactOnline.setVisibility(GONE);

			// for some reason following properties set from styles xml are not applied => apply them manually
			contactCall.setFocusable(false);
			contactCall.setFocusableInTouchMode(false);

			contactCall.setVisibility(VISIBLE);
			contactCall.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getEventManager(context).fire(call.newEvent(contact));
				}
			});

			contactDivider.setVisibility(VISIBLE);
		} else {
			contactCall.setOnClickListener(null);
			contactCall.setVisibility(GONE);
			contactDivider.setVisibility(GONE);

			if (contact.isOnline()) {
				contactOnline.setVisibility(VISIBLE);
			} else {
				contactOnline.setVisibility(GONE);
			}
		}
	}

	@Nonnull
	static Bundle newUserArguments(@Nonnull Account account, @Nonnull User user) {
		return newUserArguments(account, user.getEntity());
	}

	@Nonnull
	static Bundle newEditUserArguments(@Nonnull Account account, @Nonnull User user) {
		final Bundle arguments = newUserArguments(account, user.getEntity());
		putCreateUserFragmentClass(account, arguments);
		return arguments;
	}

	private static void putCreateUserFragmentClass(@Nonnull Account account, @Nonnull Bundle arguments) {
		final Class createUserFragmentClass = account.getRealm().getCreateUserFragmentClass();
		if (createUserFragmentClass == null) {
			throw new IllegalArgumentException("Create/edit user fragment class must be set");
		}
		arguments.putSerializable(ARG_EDIT_CLASS_NAME, createUserFragmentClass);
	}

	@Nullable
	public static Class<? extends BaseEditUserFragment> getCreateUserFragmentClassFromArguments(@Nonnull Bundle arguments) {
		final Serializable serializable = arguments.getSerializable(ARG_EDIT_CLASS_NAME);
		if (serializable instanceof Class) {
			try {
				return (Class) serializable;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			return null;
		}
	}

	@Nonnull
	static Bundle newCreateUserArguments(@Nonnull Account account) {
		final Bundle arguments = Accounts.newAccountArguments(account);
		putCreateUserFragmentClass(account, arguments);
		return arguments;
	}

	@Nonnull
	static Bundle newUserArguments(@Nonnull Account account, @Nonnull Entity user) {
		final Bundle arguments = Accounts.newAccountArguments(account);
		arguments.putString(ARG_USER_ID, user.getEntityId());
		return arguments;
	}

	@Nullable
	public static String getUserIdFromArguments(@Nonnull Bundle arguments) {
		return arguments.getString(Users.ARG_USER_ID);
	}
}
