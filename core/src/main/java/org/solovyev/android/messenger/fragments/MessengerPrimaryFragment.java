package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.FragmentDef;
import org.solovyev.android.messenger.accounts.MessengerAccountsFragment;
import org.solovyev.android.messenger.accounts.MessengerPickAccountFragment;
import org.solovyev.android.messenger.chats.MessengerRecentChatsFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.preferences.MessengerPreferenceGroupsFragment;
import org.solovyev.android.messenger.realms.MessengerRealmsFragment;
import org.solovyev.android.messenger.users.MessengerFindContactsFragment;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:04 PM
 */
public enum MessengerPrimaryFragment implements FragmentDef {

	//contacts(MessengerContactsFragment.class, Users.CONTACTS_FRAGMENT_TAG, R.string.mpp_tab_contacts),
	contacts(MessengerFindContactsFragment.class, Users.CONTACTS_FRAGMENT_TAG, R.string.mpp_tab_contacts),
	//messages(MessengerChatsFragment.class, CHATS_FRAGMENT_TAG, R.string.mpp_tab_messages),
	messages(MessengerRecentChatsFragment.class, CHATS_FRAGMENT_TAG, R.string.mpp_tab_messages),
	accounts(MessengerAccountsFragment.class, MessengerAccountsFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts),
	pick_account(MessengerPickAccountFragment.class, MessengerPickAccountFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts, true),
	realms(MessengerRealmsFragment.class, MessengerRealmsFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts, true),
	settings(MessengerPreferenceGroupsFragment.class, MessengerPreferenceGroupsFragment.FRAGMENT_TAG, R.string.mpp_tab_preferences);

	@Nonnull
	private final Class<? extends Fragment> fragmentClass;

	@Nonnull
	private final String fragmentTag;

	private final int titleResId;

	private final boolean addToBackStack;

	MessengerPrimaryFragment(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull String fragmentTag, int titleResId) {
		this(fragmentClass, fragmentTag, titleResId, false);
	}

	private MessengerPrimaryFragment(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull String fragmentTag, int titleResId, boolean addToBackStack) {
		this.fragmentClass = fragmentClass;
		this.fragmentTag = fragmentTag;
		this.titleResId = titleResId;
		this.addToBackStack = addToBackStack;
	}

	@Override
	@Nonnull
	public String getFragmentTag() {
		return this.fragmentTag;
	}

	@Override
	@Nonnull
	public Class<? extends Fragment> getFragmentClass() {
		return fragmentClass;
	}

	public int getTitleResId() {
		return titleResId;
	}

	@Override
	public boolean isAddToBackStack() {
		return addToBackStack;
	}
}
