package cz.library;

/**
 * Created by czz on 2016/8/13.
 */
public enum RefreshState {
    //start scroll state
    PULL_START,
    //start header refresh
    START_REFRESHING,
    //refresh over or cancel refresh
    RELEASE_START,
    //release the refreshing
    RELEASE_REFRESHING_START,
    //refreshing header complete
    REFRESHING_START_COMPLETE,
    //nothing to do
    NONE
}
