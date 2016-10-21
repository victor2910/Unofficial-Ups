package com.brunoaybar.unofficialupc.modules.timetable;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brunoaybar.unofficialupc.Injection;
import com.brunoaybar.unofficialupc.R;
import com.brunoaybar.unofficialupc.data.models.Timetable;
import com.brunoaybar.unofficialupc.modules.base.BaseFragment;
import com.brunoaybar.unofficialupc.modules.courses.CourseDetailActivity;
import com.brunoaybar.unofficialupc.modules.courses.CoursesViewModel;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimetableFragment extends BaseFragment {

    @Nullable
    private TimetableViewModel mViewModel;
    private CoursesViewModel mCourseViewModel;
    @Nullable
    private Timetable mTimetable;
    private Date mSelectedDate;

    public static TimetableFragment newInstance(){
        return new TimetableFragment();
    }

    public TimetableFragment() {
        setFragmentTitle(R.string.option_timetable);
    }

    @BindView(R.id.weekDatePicker) WeekDatePicker weekDatePicker;
    @BindView(R.id.dayScheduleView) DayScheduleView dayScheduleView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialize ViewModels
        mViewModel = new TimetableViewModel(Injection.provideUpcRepository(getContext()));
        mCourseViewModel = new CoursesViewModel(Injection.provideUpcRepository(getContext()));
        //Inflate view
        View v  = inflater.inflate(R.layout.fragment_timetable, container, false);
        ButterKnife.bind(this,v);

        weekDatePicker.setListener(this::updateScheduleForDay);
        dayScheduleView.setListener(this::openCourse);

        if(mTimetable == null){
            mSubscription.add(mViewModel.getTimetable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setTimetable,this::displayError));
        }else{
            if(mSelectedDate != null)
                weekDatePicker.setSelectedDate(mSelectedDate);
            setTimetable(mTimetable);
        }

        return v;
    }

    private void setTimetable(Timetable timetable){
        mTimetable = timetable;
        updateScheduleForDay(weekDatePicker.getSelectedDate());
    }

    private void updateScheduleForDay(Date selectedDate){
        mSelectedDate = selectedDate;
        if(mTimetable==null)
            return;
        dayScheduleView.setDay(mTimetable.getDay(selectedDate));
    }

    private void openCourse(Timetable.Class mClass){
        assert mViewModel!=null && mCourseViewModel!=null;

        mSubscription.add(mCourseViewModel.getCourseDetails().subscribe(bundle -> {
            Intent i = new Intent(getActivity(),CourseDetailActivity.class);
            i.putExtras(bundle);
            startActivity(i);
        }));

        mViewModel.getCourseFromClass(mClass).subscribe(mCourseViewModel::courseAvailable);
    }

}