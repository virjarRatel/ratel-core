#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
res_dir=`pwd`/res

ratel_version=`cat ${res_dir}/ratel_version.txt`
builder_jar=${res_dir}/${ratel_version}

if [ -f ${builder_jar} ] ;then
    echo "use ${builder_jar}"
else
    echo "can not find container build jar in path:${builder_jar}"
    echo -1
fi

cd ${now_dir}

has_certificate='false'
is_extract_apk_task='false'
is_help='false'

for ratel_param in $@
do
    # echo ${ratel_param}
    if [ ${ratel_param} = '-c' ] ;then
        has_certificate='true'
    elif [ ${ratel_param} = '--certificate' ] ;then
         has_certificate='true'
    elif [ ${ratel_param} = '-x' ] ;then
         is_extract_apk_task='true'
    elif [ ${ratel_param} = '--extract' ] ;then
         is_extract_apk_task='true'
    elif [ ${ratel_param} = '--help' ] ;then
         is_help='true'
    elif [ ${ratel_param} = '-h' ] ;then
         is_help='true'
    fi
done

out_apk_path=`java -jar ${builder_jar} -t -s -w ~/.ratel-working-repkg -c ${res_dir}/monthly_temp.txt $*`
if [ $? != 0 ] ;then
    echo "call builder jar failed"
    exit $?
fi

if [ ${is_help} = 'false' ] ;then
    echo "assemble new apk for $*"
fi


if [ ${has_certificate} = 'true' ] ; then
    java -jar ${builder_jar} -s -w ~/.ratel-working-repkg $*
else
    if [ ${is_help} = 'false' ] ;then
      echo "build with default certificate"
    fi
    java -jar ${builder_jar} -s -w ~/.ratel-working-repkg -c ${res_dir}/monthly_temp.txt $*
fi
if [ $? != 0 ] ;then
    echo "assemble ratel apk failed"
    exit $?
fi

if [ ${is_extract_apk_task} = 'true' ] ;then
    exit 0
fi
if [ ${is_help} = 'true' ] ;then
    exit 0
fi
echo "the final output apk file is :${out_apk_path}"

