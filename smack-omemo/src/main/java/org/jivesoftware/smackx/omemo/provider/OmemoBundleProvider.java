/**
 *
 * Copyright the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.omemo.provider;

import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smackx.omemo.elements.OmemoBundleElement;
import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;

import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.SIGNED_PRE_KEY_ID;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.SIGNED_PRE_KEY_PUB;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.SIGNED_PRE_KEY_SIG;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.IDENTITY_KEY;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.PRE_KEYS;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.PRE_KEY_PUB;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.PRE_KEY_ID;
import static org.jivesoftware.smackx.omemo.util.OmemoConstants.Bundle.BUNDLE;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

/**
 * Smack ExtensionProvider that parses OMEMO bundle elements into OmemoBundleElement objects.
 *
 * @author Paul Schaub
 */
public class OmemoBundleProvider extends ExtensionElementProvider<OmemoBundleElement> {
    @Override
    public OmemoBundleElement parse(XmlPullParser parser, int initialDepth) throws Exception {
        boolean stop = false;
        boolean inPreKeys = false;

        int signedPreKeyId = -1;
        byte[] signedPreKey = null;
        byte[] signedPreKeySignature = null;
        byte[] identityKey = null;
        HashMap<Integer, byte[]> preKeys = new HashMap<>();

        while (!stop) {
            int tag = parser.next();
            String name = parser.getName();
            switch (tag) {
                case START_TAG:
                    // <signedPreKeyPublic>
                    if (name.equals(SIGNED_PRE_KEY_PUB)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equals(SIGNED_PRE_KEY_ID)) {
                                int id = Integer.parseInt(parser.getAttributeValue(i));
                                signedPreKey = Base64.decode(parser.nextText());
                                signedPreKeyId = id;
                            }
                        }
                    }
                    // <bundleGetSignedPreKeySignature>
                    else if (name.equals(SIGNED_PRE_KEY_SIG)) {
                        signedPreKeySignature = Base64.decode(parser.nextText());
                    }
                    // <deserializeIdentityKey>
                    else if (name.equals(IDENTITY_KEY)) {
                        identityKey = Base64.decode(parser.nextText());
                    }
                    // <deserializeECPublicKeys>
                    else if (name.equals(PRE_KEYS)) {
                        inPreKeys = true;
                    }
                    // <preKeyPublic preKeyId='424242'>
                    else if (inPreKeys && name.equals(PRE_KEY_PUB)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equals(PRE_KEY_ID)) {
                                preKeys.put(Integer.parseInt(parser.getAttributeValue(i)),
                                        Base64.decode(parser.nextText()));
                            }
                        }
                    }
                    break;
                case END_TAG:
                    if (name.equals(BUNDLE)) {
                        stop = true;
                    }
                    break;
            }
        }
        return new OmemoBundleElement(signedPreKeyId, signedPreKey, signedPreKeySignature, identityKey, preKeys);
    }
}
