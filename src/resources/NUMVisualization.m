% mutation max value visualization
clear;close all;
mop.name='DTLZ1';
mop.obj_nums=3;
nrun=19;
for i=0:19
    R='R';
    A=load(strcat('MValue/','mutedValue_',mop.name,'(',int2str(mop.obj_nums),')_',strcat(R,num2str(i)),'.dat'));
    leng=length(A);
    x=1/leng:1/leng:1;
    plot(x,A),hold on;
end
 xhandle=xlabel('t /T');
 yhandle=ylabel('max mutation value');
set(xhandle,'Fontname','times new Roman','FontSize',10,'FontWeight','bold');
set(yhandle,'Fontname','times new Roman','FontSize',10,'FontWeight','bold');
set(gca,'Fontname','times new Roman','FontSize',10,'FontWeight','bold');
%set(gca,'yscale','log');
