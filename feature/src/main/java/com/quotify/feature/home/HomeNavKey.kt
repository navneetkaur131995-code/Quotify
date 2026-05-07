package com.quotify.feature.home

import com.quotify.core.navigation.QuotifyNavKey
import kotlinx.serialization.Serializable

/*
Role: The "Menu" of screens inside the Home feature.
• This is a sealed interface that lists every possible screen that belongs to the Home module.
• Right now, you only have QuoteList, but if you added a "Favorites" tab or a "Search" screen within the Home feature,
you would add them here.
• Think of this as: "Everything that can happen inside the Home feature."

Analogy: The Envelope
• HomeNavKeys.QuoteList is the Letter (the actual content).
• HomeNavKeys is the Type of Letter (it must be a "Home" letter).
• HomeNavKey is the Envelope.
The Mailman (the :app module) doesn't need to know what's in the letter; he just needs to know how to handle
the Envelope. Only when the envelope reaches the Home feature's office (HomeEntryBuilder)
is it opened to see which screen to show.
* */
@Serializable
sealed interface HomeNavKeys : QuotifyNavKey {
    /* Role: A specific destination.
    • This is the actual "ID" for the quote list screen. Since the list doesn't need any arguments (like an ID),
    it's a data object.
    • Think of this as: "The specific instruction to show the List."
     */
    @Serializable
    data object QuoteList : HomeNavKeys
}

/* Role: The "Public Passport" for the whole module.
• This is a simple wrapper that holds one of the HomeNavKeys.
• The "Why": This is the most important part. By using this wrapper, you only have to register one class (HomeNavKey)
in your :app module's QuotifyNavConfiguration.
• If you didn't have this wrapper, and the Home feature had 10 different screens, you would have to manually add all 10
 of them to the :app module's serialization list.
  With the wrapper, the :app module says: "I know how to handle HomeNavKey." The Home module then handles the internal
   details of which specific screen (HomeNavKeys) is inside it.*/
@Serializable
data class HomeNavKey(
    val key: HomeNavKeys,
) : HomeNavKeys
