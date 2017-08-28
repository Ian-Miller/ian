package ian.a;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ian on 8/9/2017.
 */

class AAdapter extends PagerAdapter {
    private View[] mPages;

    AAdapter(View... pages) {
        mPages = pages;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View child = mPages[position];
        if(child.getParent() == null) {
            container.addView(child);
        }
        return child;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        if(object instanceof View){
//            final View parent = (View) object;
//            container.removeView(parent);
//        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        Object tag = mPages[position].getTag();
        if(tag != null){
            try{
                title = (String) tag;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return title;
    }

    @Override
    public int getCount() {
        return mPages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
