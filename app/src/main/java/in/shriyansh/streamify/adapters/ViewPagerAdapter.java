package in.shriyansh.streamify.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.shriyansh.streamify.fragments.Events;
import in.shriyansh.streamify.fragments.News;
import in.shriyansh.streamify.fragments.Dashboard;

/**
 * Created by shriyansh on 7/9/15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    // This will Store the Titles of the Tabs which are Going to be passed when
    // ViewPagerAdapter is created
    private final CharSequence[] titles;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence[] titles) {
        super(fm);
        this.titles = titles;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                // if the position is 0 we are returning the First tab
                return new News();
            case 1:
                return new Events();
            default:
                return new Dashboard();
        }
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public void setTitles(CharSequence newTitle,int position) {
        this.titles[position] = newTitle;
    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return this.titles.length;
    }

}
