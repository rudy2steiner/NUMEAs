%parallel coordinate fig
clear
mop.name='DTLZ2';
mop.obj_nums=10;
R='R9';
A=load(strcat('pf_solution/','PFs_',mop.name,'(',int2str(mop.obj_nums),')_',R,'.dat'));  
 xaxis=1:mop.obj_nums;
 disp(xaxis);
 %c=jet(length(A));
 n='bgrcmykw';
 for k=1:length(A) 
     disp(A(k,:));
    % plot(xaxis,A(k,:),'Color',[rand(),0.5+0.5*rand(),rand()]),hold on; 
    plot(xaxis,A(k,:),strcat('-',n(mod(k,7)+1)),'linewidth',1.4),hold on; 
 end
 axis([1 mop.obj_nums 0 1]);
 set(gca,'ytick',0:0.2:1);

 %title(strcat(mop.name,'参考点分布情况'));
xlabel('Objective NO');                                                         
ylabel('Objective Value');