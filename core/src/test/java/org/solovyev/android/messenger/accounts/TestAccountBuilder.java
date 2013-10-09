package org.solovyev.android.messenger.accounts;


import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class TestAccountBuilder extends AbstractAccountBuilder<TestAccount, TestAccountConfiguration> {

	public TestAccountBuilder(@Nonnull Realm realm, @Nonnull TestAccountConfiguration configuration, @Nullable TestAccount editedAccount) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		final TestAccountConfiguration configuration = getConfiguration();
		String accountEntityId = "test_user";

		final Integer accountUserId = configuration.getAccountUserId();
		if(accountUserId != null) {
			accountEntityId += "_" + accountUserId;
		}
		final MutableUser user = newEmptyUser(newEntity(accountId, accountEntityId));
		user.setOnline(true);
		return user;
	}

	@Nonnull
	@Override
	protected TestAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state) {
		return new TestAccount(id, getRealm(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {

	}

	@Override
	public void disconnect() throws ConnectionException {

	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {

	}
}
