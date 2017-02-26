package org.thoughtcrime.securesms.crypto.storage;

import android.content.Context;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil;
import org.thoughtcrime.securesms.crypto.SessionUtil;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.jobs.IdentityUpdateJob;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

public class TextSecureIdentityKeyStore implements IdentityKeyStore {

  private final Context context;

  public TextSecureIdentityKeyStore(Context context) {
    this.context = context;
  }

  @Override
  public IdentityKeyPair getIdentityKeyPair() {
    return IdentityKeyUtil.getIdentityKeyPair(context);
  }

  @Override
  public int getLocalRegistrationId() {
    return TextSecurePreferences.getLocalRegistrationId(context);
  }

  @Override
  public void saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    long recipientId = RecipientFactory.getRecipientsFromString(context, address.getName(), true).getPrimaryRecipient().getRecipientId();
    DatabaseFactory.getIdentityDatabase(context).saveIdentity(recipientId, identityKey);
  }

  @Override
  public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    long    recipientId = RecipientFactory.getRecipientsFromString(context, address.getName(), true).getPrimaryRecipient().getRecipientId();
    boolean trusted     = DatabaseFactory.getIdentityDatabase(context)
                                         .isValidIdentity(recipientId, identityKey);

    if (trusted) {
      return true;
    } else if (!TextSecurePreferences.isBlockingIdentityUpdates(context)) {
      saveIdentity(address, identityKey);
      new TextSecureSessionStore(context).deleteAllSessions(address.getName());

      ApplicationContext.getInstance(context)
                        .getJobManager()
                        .add(new IdentityUpdateJob(context, recipientId));

      return true;
    } else {
      return false;
    }
  }
}
