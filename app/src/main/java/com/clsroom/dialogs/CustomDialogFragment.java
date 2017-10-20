package com.clsroom.dialogs;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.clsroom.R;
import com.clsroom.model.Classes;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.clsroom.R.id.dialogSubTitle;

public abstract class CustomDialogFragment extends DialogFragment implements TabLayout.OnTabSelectedListener
{
    private TextView mDialogTitle;
    private ImageView mSubmitBtnImg;
    private TextView mSubmitBtnTxt;
    private View mSubmitBtn;
    private TextView mSubTitle;
    private View mBottomPadding;
    private TabLayout mTabLayout;
    private LinearLayout mDialogContainer;
    private View mDialogFabContainer;
    private FloatingActionButton mDialogFab;

    private View mSubmitBtn2;
    private ImageView mSubmitBtn1Img;
    private ImageView mSubmitBtn2Img;

    private TextView mSubmitBtn1Txt;
    private TextView mSubmitBtn2Txt;

    public CustomDialogFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if (window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }

        View parentView = inflater.inflate(R.layout.fragment_custom_dialog, container, false);
        mDialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);

        mSubmitBtnImg = (ImageView) parentView.findViewById(R.id.submitBtnImg);
        mSubmitBtnTxt = (TextView) parentView.findViewById(R.id.submitBtnTxt);

        mSubmitBtn = parentView.findViewById(R.id.singleSubmitBtnContainer);

        mSubmitBtn1Img = (ImageView) parentView.findViewById(R.id.submitBtn1Img);
        mSubmitBtn1Txt = (TextView) parentView.findViewById(R.id.submitBtn1Txt);

        mSubmitBtn2Img = (ImageView) parentView.findViewById(R.id.submitBtn2Img);
        mSubmitBtn2Txt = (TextView) parentView.findViewById(R.id.submitBtn2Txt);

        mSubmitBtn2 = parentView.findViewById(R.id.dualSubmitButtonContainer);

        mBottomPadding = parentView.findViewById(R.id.bottomPadding);
        mSubTitle = (TextView) parentView.findViewById(dialogSubTitle);
        mTabLayout = (TabLayout) parentView.findViewById(R.id.classesTab);
        mDialogFabContainer = parentView.findViewById(R.id.dialogFabContainer);
        mDialogFab = (FloatingActionButton) parentView.findViewById(R.id.dialogFab);
        parentView.findViewById(R.id.closeDialog).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        parentView.findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                submit(view);
            }
        });

        parentView.findViewById(R.id.submitBtn1).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                submit1(view);
            }
        });

        parentView.findViewById(R.id.submitBtn2).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                submit2(view);
            }
        });

        mDialogContainer = (LinearLayout) parentView.findViewById(R.id.dialogContainer);
        mDialogContainer.removeAllViews();
        mDialogContainer.addView(inflater.inflate(getContentViewLayoutRes(), null));
        onCreateView(mDialogContainer);
        return parentView;
    }

    protected void submit1(View view)
    {
    }

    protected void submit2(View view)
    {
    }

    public abstract void onCreateView(View parentView);

    protected abstract int getContentViewLayoutRes();

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    public void setDialogTitle(String title)
    {
        mDialogTitle.setText(title);
    }

    public void setDialogTitle(int title)
    {
        mDialogTitle.setText(title);
    }

    public void setSubmitBtnTxt(String title)
    {
        mSubmitBtnTxt.setText(title);
        showSubmitButton();
    }

    public void setSubmitBtnTxt(int title)
    {
        mSubmitBtnTxt.setText(title);
        showSubmitButton();
    }

    public void setSubmitBtnImg(int imgResId)
    {
        mSubmitBtnImg.setImageResource(imgResId);
        showSubmitButton();
    }

    public void showSubmitButton()
    {
        mSubmitBtn.setVisibility(View.VISIBLE);
        mSubmitBtn2.setVisibility(View.GONE);
    }

    public void setSubmitBtn1Txt(String title)
    {
        mSubmitBtn1Txt.setText(title);
        showDualSubmitButton();
    }

    public void setSubmitBtn1Txt(int title)
    {
        mSubmitBtn1Txt.setText(title);
        showDualSubmitButton();
    }

    public void setSubmitBtn1Img(int imgResId)
    {
        mSubmitBtn1Img.setImageResource(imgResId);
        showDualSubmitButton();
    }

    public void showDualSubmitButton()
    {
        mSubmitBtn2.setVisibility(View.VISIBLE);
        mSubmitBtn.setVisibility(View.GONE);
    }

    public void setSubmitBtn2Txt(String title)
    {
        mSubmitBtn2Txt.setText(title);
        showDualSubmitButton();
    }

    public void setSubmitBtn2Txt(int title)
    {
        mSubmitBtn2Txt.setText(title);
        showDualSubmitButton();
    }

    public void setSubmitBtn2Img(int imgResId)
    {
        mSubmitBtn2Img.setImageResource(imgResId);
        showDualSubmitButton();
    }

    public void submit(View view)
    {

    }

    public void setSubTitle(String title)
    {
        mSubTitle.setVisibility(View.VISIBLE);
        mBottomPadding.setVisibility(View.GONE);
        mSubTitle.setText(title);
    }

    public void setSubTitle(int title)
    {
        mSubTitle.setVisibility(View.VISIBLE);
        mBottomPadding.setVisibility(View.GONE);
        mSubTitle.setText(title);
    }

    public void showClassesTab()
    {
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference().child(Classes.CLASSES);
        mTabLayout.setVisibility(View.VISIBLE);
        mTabLayout.removeAllTabs();
        mTabLayout.addOnTabSelectedListener(this);

        classesRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Classes classes = snapshot.getValue(Classes.class);
                    TabLayout.Tab tab = mTabLayout.newTab();
                    tab.setText(classes.getName());
                    tab.setTag(classes);
                    mTabLayout.addTab(tab);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    public void setContainerHeight()
    {
        ViewGroup.LayoutParams params = mDialogContainer.getLayoutParams();
        params.height = MATCH_PARENT;
        mDialogContainer.setLayoutParams(params);
    }

    public void setDialogFabImg(int dialogFabImg)
    {
        mDialogFabContainer.setVisibility(View.VISIBLE);
        mDialogFab.setImageResource(dialogFabImg);
    }
}