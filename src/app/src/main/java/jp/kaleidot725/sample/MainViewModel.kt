package jp.kaleidot725.sample

import android.util.Log
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent

class MainViewModel : ViewModel() {
    private val _event: LiveEvent<Event> = LiveEvent()
    val event: LiveData<Event> = _event

    private val _userToken: MutableLiveData<String> = MutableLiveData(UNKNOWN_USER)
    val userToken: LiveData<String> = _userToken.map { it ?: UNKNOWN_USER }
    var authorized : Boolean = false
    val actionName : LiveData<String> = _userToken.map { if (it != UNKNOWN_USER) "Sign Out" else "Sign In" }

    init {
        userToken.observeForever {
            authorized =  it != UNKNOWN_USER
        }
    }

    fun update(userToken: String?) {
        _userToken.value = userToken ?: UNKNOWN_USER
    }

    fun reset() {
        _userToken.value = UNKNOWN_USER
    }

    fun action() {
        if (!authorized) {
            _event.value = Event.SIGN_IN
        } else {
            _event.value = Event.SIGN_OUT
        }
    }

    enum class Event {
        SIGN_IN,
        SIGN_OUT
    }

    companion object {
        private const val UNKNOWN_USER = "Unknown"
    }
}