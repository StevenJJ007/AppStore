package com.anyonavinfo.cpadstore.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anyonavinfo.cpadstore.appFragment.AppFragment;
import com.anyonavinfo.cpadstore.manageFragment.MangerFragment;

public class MFragmentPagerAdapter extends FragmentPagerAdapter{

	public MFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {

		Fragment fragment = null;
		switch (position) {
			case 0:
				fragment = new AppFragment();
				break;

			case 1:
				fragment = new MangerFragment();
				break;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 2;
	}
}
