package com.example.quotify.core.di

import com.example.quotify.core.data.repository.QuoteRepositoryImpl
import com.example.quotify.core.domain.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/* When to use @Provides vs @Binds
*  @Binds when you have an interface → implementation
* Use @Binds when
    - You have an interface
    - You already have a concrete implementation
    - The implementation has an @Inject constructor
   Rules - Must be in abstract class & function must be abstract
        - Faster & cleaner than @Provides

* Use @Provides when you need to create the object yourself
    - Use @Provides when
    - You cannot use @Inject constructor
    - The object comes from a library
    - You need custom construction logic

* Do I need to call new / Builder() myself?
Yes → @Provides
No → @Binds

* */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule(){

    @Binds
    abstract fun bindsQuoteRepository(quoteRepositoryImpl: QuoteRepositoryImpl): QuoteRepository
}