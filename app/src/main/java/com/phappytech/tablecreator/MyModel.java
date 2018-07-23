package com.phappytech.tablecreator;

import android.os.Parcel;
import android.os.Parcelable;

import org.chalup.microorm.annotations.Column;

public class MyModel implements Parcelable {
    @Column("empID")
    int id;
    @Column("name")
    String name;
    @Column("isPresent")
    boolean isPresent;
    @Column("salary")
    float salaryInLacs;

    public MyModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        isPresent = in.readByte() != 0;
        salaryInLacs = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeByte((byte) (isPresent ? 1 : 0));
        dest.writeFloat(salaryInLacs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyModel> CREATOR = new Creator<MyModel>() {
        @Override
        public MyModel createFromParcel(Parcel in) {
            return new MyModel(in);
        }

        @Override
        public MyModel[] newArray(int size) {
            return new MyModel[size];
        }
    };
}
